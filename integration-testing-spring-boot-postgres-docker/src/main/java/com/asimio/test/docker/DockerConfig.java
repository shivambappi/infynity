package com.asimio.test.docker;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
@Documented
// Uses DOCKER_HOST and DOCKER_CERT_PATH env vars to instantiate docker client or uri attribute
public @interface DockerConfig {

	RegistryConfig registry();
	String host() default "";
	String image();
	int[] containerToHostRandomPorts();
	boolean waitForPorts() default false;
	ContainerStartMode startMode() default ContainerStartMode.FOR_EACH_TEST;

	enum ContainerStartMode {
		ONCE,
		FOR_EACH_TEST
	}
}