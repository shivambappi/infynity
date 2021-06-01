package com.asimio.test.docker.spring;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import com.asimio.test.docker.DockerConfig;
import com.asimio.test.docker.RegistryConfig;
import com.asimio.test.docker.utils.NetworkUtil;
import com.asimio.test.docker.utils.ObjectUtil;
import com.asimio.test.docker.utils.TestContextUtil;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DefaultDockerClient.Builder;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.RemoveContainerParam;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.messages.AuthConfig;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

public class DockerizedTestExecutionListener extends AbstractTestExecutionListener {

	private static final Logger LOG = LoggerFactory.getLogger(DockerizedTestExecutionListener.class);
	private static final int DEFAULT_PORT_WAIT_TIMEOUT_IN_MILLIS = 4000;
	private static final int DEFAULT_STOP_WAIT_BEFORE_KILLING_CONTAINER_IN_SECONDS = 2;
	private static final String HOST_PORT_SYS_PROPERTY_NAME_PATTERN = "HOST_PORT_FOR_%s";

	private DockerClient docker;
	private Set<String> containerIds = Sets.newConcurrentHashSet();

	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		final DockerConfig dockerConfig = (DockerConfig) TestContextUtil.getClassAnnotationConfiguration(testContext, DockerConfig.class);
		this.validateDockerConfig(dockerConfig);

		final String image = dockerConfig.image();
		this.docker = this.createDockerClient(dockerConfig);
		LOG.debug("Pulling image '{}' from Docker registry ...", image);
		this.docker.pull(image);
		LOG.debug("Completed pulling image '{}' from Docker registry", image);

		if (DockerConfig.ContainerStartMode.ONCE == dockerConfig.startMode()) {
			this.startContainer(testContext);
		}

		super.beforeTestClass(testContext);
	}

	@Override
	public void prepareTestInstance(TestContext testContext) throws Exception {
		final DockerConfig dockerConfig = (DockerConfig) TestContextUtil.getClassAnnotationConfiguration(testContext, DockerConfig.class);
		if (DockerConfig.ContainerStartMode.FOR_EACH_TEST == dockerConfig.startMode()) {
			this.startContainer(testContext);
		}
		super.prepareTestInstance(testContext);
	}

	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
		try {
			super.afterTestClass(testContext);
			for (String containerId : this.containerIds) {
				LOG.debug("Stopping container: {}, timeout to kill: {}", containerId, DEFAULT_STOP_WAIT_BEFORE_KILLING_CONTAINER_IN_SECONDS);
				this.docker.stopContainer(containerId, DEFAULT_STOP_WAIT_BEFORE_KILLING_CONTAINER_IN_SECONDS);
				LOG.debug("Removing container: {}", containerId);
				this.docker.removeContainer(containerId, RemoveContainerParam.forceKill());
			}
		} finally {
			LOG.debug("Final cleanup");
			IOUtils.closeQuietly(this.docker);
		}
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	private DockerClient createDockerClient(DockerConfig dockerConfig) throws DockerCertificateException {
		final AuthConfig registryAuthConfig = this.getRegistryAuthConfig(dockerConfig.registry());
		Builder dockerBuilder = null;
		if (System.getenv("DOCKER_HOST") != null) {
			dockerBuilder = DefaultDockerClient.fromEnv();
		} else {
			dockerBuilder = DefaultDockerClient.builder().uri(URI.create(dockerConfig.host()));
		}
		return dockerBuilder.authConfig(registryAuthConfig).build();
	}

	private final Map<String, List<PortBinding>> bindContainerToHostRandomPorts(DockerClient docker, int[] ports) {
		final Map<String, List<PortBinding>> result = new HashMap<String, List<PortBinding>>();
		for (int port : ports) {
			result.put(String.valueOf(port), Arrays.asList(PortBinding.randomPort("0.0.0.0")));
		}
		return result;
	}

	private AuthConfig getRegistryAuthConfig(RegistryConfig registryConfig) {
		if (ObjectUtil.allNullOrEmpty(registryConfig.email(), registryConfig.userName(), registryConfig.passwd())) {
			return null;
		}
		final AuthConfig result = AuthConfig.builder().
			email(registryConfig.email()).
			username(registryConfig.userName()).
			password(registryConfig.passwd()).
			serverAddress(registryConfig.host()).
			build();
		return result;
	}

	private void validateDockerConfig(DockerConfig dockerConfig) {
		if (StringUtils.isBlank(dockerConfig.image())) {
			throw new RuntimeException("Cannot execute test, image is a required attribute in " + DockerConfig.class.getSimpleName());
		}
	}

	private void startContainer(TestContext testContext) throws Exception {
		LOG.debug("Starting docker container in prepareTestInstance() to make System properties available to Spring context ...");
		final DockerConfig dockerConfig = (DockerConfig) TestContextUtil.getClassAnnotationConfiguration(testContext, DockerConfig.class);
		final String image = dockerConfig.image();

		// Bind container ports to automatically allocated available host ports
		final int[] containerToHostRandomPorts = dockerConfig.containerToHostRandomPorts();
		final Map<String, List<PortBinding>> portBindings = this.bindContainerToHostRandomPorts(this.docker, containerToHostRandomPorts);

		// Creates container with exposed ports, makes host ports available to outside
		final HostConfig hostConfig = HostConfig.builder().
			portBindings(portBindings).
			publishAllPorts(true).
			build();
		final ContainerConfig containerConfig = ContainerConfig.builder().
			hostConfig(hostConfig).
			image(image).
			build();

		LOG.debug("Creating container for image: {}", image);
		final ContainerCreation creation = this.docker.createContainer(containerConfig);
		final String id = creation.id();
		LOG.debug("Created container [image={}, containerId={}]", image, id);

		// Stores container Id to remove it for later removal
		this.containerIds.add(id);

		// Starts container
		this.docker.startContainer(id);
		LOG.debug("Started container: {}", id);

		Set<String> hostPorts = Sets.newHashSet();

		// Sets published host ports to system properties so that test method can connect through it
		final ContainerInfo info = this.docker.inspectContainer(id);
		final Map<String, List<PortBinding>> infoPorts = info.networkSettings().ports();
		for (int port : containerToHostRandomPorts) {
			final String hostPort = infoPorts.get(String.format("%s/tcp", port)).iterator().next().hostPort();
			hostPorts.add(hostPort);
			final String hostToContainerPortMapPropName = String.format(HOST_PORT_SYS_PROPERTY_NAME_PATTERN, port);
			System.getProperties().put(hostToContainerPortMapPropName, hostPort);
			LOG.debug(String.format("Mapped ports host=%s to container=%s via System property=%s", hostPort, port, hostToContainerPortMapPropName));
		}

		// Makes sure ports are LISTENing before giving running test
		if (dockerConfig.waitForPorts()) {
			LOG.debug("Waiting for host ports [{}] ...", StringUtils.join(hostPorts, ", "));
			final Collection<Integer> intHostPorts = Collections2.transform(hostPorts,
				new Function<String, Integer>() {

					@Override
					public Integer apply(String arg) {
						return Integer.valueOf(arg);
					}
				}
			);
			NetworkUtil.waitForPort(this.docker.getHost(), intHostPorts, DEFAULT_PORT_WAIT_TIMEOUT_IN_MILLIS);
			LOG.debug("All ports are now listening");
		}
	}
}