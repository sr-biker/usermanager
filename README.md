# Digital Servicing User Manager
## IMPORTANT
Access token `usermanager.imgur.accessToken ` and `usermanager.kafka.password` are not checked in 
for security reasons and is sent via email.

## Requirements

For building and running the application locally you need:

- [JDK 21](https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html)
- [Maven 3](https://maven.apache.org)
- `chmod +x setup_kafka.sh; ./setup_kafka.sh ` on the same JVM as the application is going to run.

For deploying the application you need:
- [Docker](https://www.docker.com)

## Running the application locally

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method in the `UserManagerApplication` class from your IDE.

Alternatively you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:

```shell
 mvn clean test spring-boot:run
```

## Deploying the application 

To deploy the usermanager application, package the application and deploy it in the Jenkins server(not included) via the Jenkins file

```shell
mvn spring-boot:build-image 
docker run docker.io/library/usermanager:0.0.1-SNAPSHOT
```
Alternately, use docker to build and tag

```shell
docker build .
docker tag <imageid> usermanager:latest
docker run usermanager:latest
```

## API Docs

Afer starting the application, openapi docs are accessible via the following link

[Open API](http://localhost:8080/api-docs)

## Copyright

Released under the Apache License 2.0. See the [LICENSE](https://github.com/codecentric/springboot-sample-app/blob/master/LICENSE) file.