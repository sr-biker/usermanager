# Digital Servicing User Manager

## Requirements

For building and running the application locally you need:

- [JDK 21](https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html)
- [Maven 3](https://maven.apache.org)

For deploying the application you need:
- [Docker](https://www.docker.com)

## Running the application locally

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method in the `UserManagerApplication` class from your IDE.

Alternatively you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:

```shell
 mvn clean test spring-boot:run
```

## Deploying the application 

To deploy the usermanager application, do  is having a docker image (and possibly deploy to Jenkins or any other pipeline of your choice):

```shell
docker build .
```

This will create:

* A docker image called "springboot-maven3-centos"


If you want to access the app from outside your OpenShift installation, you have to expose the springboot-sample-app service:

```shell
oc expose springboot-sample-app --hostname=www.example.com
```

## Copyright

Released under the Apache License 2.0. See the [LICENSE](https://github.com/codecentric/springboot-sample-app/blob/master/LICENSE) file.