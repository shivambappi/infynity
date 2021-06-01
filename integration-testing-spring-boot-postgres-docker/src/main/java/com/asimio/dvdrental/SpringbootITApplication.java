package com.asimio.dvdrental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = { "com.asimio.dvdrentals.model" })
@EnableJpaRepositories(basePackages = { "com.asimio.dvdrentals.dao" })
public class SpringbootITApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootITApplication.class, args);
	}
}