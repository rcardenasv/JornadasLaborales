FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /deployments
COPY --from=build --chown=1001:0 /workspace/target/quarkus-app/ ./
USER 1001
EXPOSE 8080
CMD ["java", "-Djava.util.logging.manager=org.jboss.logmanager.LogManager", "-jar", "quarkus-run.jar"]