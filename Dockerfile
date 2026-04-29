# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -Dmaven.test.skip=true -Dcheckstyle.skip=true -Dfmt.skip=true package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/target/ai-kb-demo-server.jar ./app.jar
COPY --from=build /workspace/target/classes/com/brolei/aikb/ContainerHealthCheck.class ./healthcheck/com/brolei/aikb/ContainerHealthCheck.class

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Dfile.encoding=UTF-8", "-jar", "/app/app.jar"]
