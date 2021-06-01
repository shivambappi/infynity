package com.asimio.dvdrental.dao;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import com.asimio.dvdrental.SpringbootITApplication;
import com.asimio.dvdrental.dao.ActorDao;
import com.asimio.test.docker.DockerConfig;
import com.asimio.test.docker.DockerConfig.ContainerStartMode;
import com.asimio.test.docker.RegistryConfig;
import com.asimio.test.docker.spring.DockerizedTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringbootITApplication.class)
@TestExecutionListeners({
	DockerizedTestExecutionListener.class,
	DependencyInjectionTestExecutionListener.class,
	DirtiesContextTestExecutionListener.class
})
@DockerConfig(image = "asimio/db_dvdrental:latest",
	containerToHostRandomPorts = { 5432 }, waitForPorts = true, startMode = ContainerStartMode.FOR_EACH_TEST,
	registry = @RegistryConfig(email="", host="", userName="", passwd="")
)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles(profiles = { "test" })
public class ActorDaoIT {

	@Autowired
	private ActorDao actorDao;

	@Test
	public void shouldHave200Actors_1() {
		Assert.assertThat(this.actorDao.count(), Matchers.equalTo(200L));		
	}

	@Test
	public void shouldHave200Actors_2() {
		Assert.assertThat(this.actorDao.count(), Matchers.equalTo(200L));		
	}

	@Test
	public void shouldHave200Actors_3() {
		Assert.assertThat(this.actorDao.count(), Matchers.equalTo(200L));		
	}
}