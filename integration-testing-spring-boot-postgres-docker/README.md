# README #

Accompanying source code for blog entry at http://tech.asimio.net/2016/08/04/Integration-Testing-using-Spring-Boot-Postgres-and-Docker.html

### Requirements ###

* Java 8
* Maven 3.3.x
* Docker host or Docker machine

### Environment variables ###
```
export DOCKER_HOST=172.16.69.133:2376
export DOCKER_MACHINE_NAME=osxdocker
export DOCKER_TLS_VERIFY=1
export DOCKER_CERT_PATH=~/.docker/machine/certs
```

They are used by Spotify's docker-client.

### Building and running the integration tests ###
```
mvn clean verify
```

### Who do I talk to? ###

* ootero at asimio dot net
* https://www.linkedin.com/in/ootero