FROM openjdk:21-slim

# Make port 8080 available to the world outside this container
EXPOSE 8080
ARG JAR_FILE=target/usermanager-0.0.1-SNAPSHOT.jar
WORKDIR /opt/app

COPY ${JAR_FILE} usermanager.jar

# Run the jar file
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","usermanager.jar"]