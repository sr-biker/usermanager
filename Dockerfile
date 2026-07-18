FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre-alpine
# Make port 8080 available to the world outside this container
EXPOSE 8080
WORKDIR /opt/app
COPY --from=build /build/target/usermanager-0.0.1-SNAPSHOT.jar usermanager.jar

# Run the jar file
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","usermanager.jar"]
