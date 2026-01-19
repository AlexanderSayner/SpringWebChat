#FROM gradle:9.2.1-jdk25 AS build
FROM gradle:8.14.3-jdk21-graal AS build

WORKDIR /opt/app

COPY build.gradle.kts .
COPY settings.gradle.kts .

COPY src/ ./src/

RUN gradle bootJar

RUN java -Djarmode=layertools -jar build/libs/*.jar extract


FROM openjdk:21-ea-21-bullseye

WORKDIR /opt/app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]
