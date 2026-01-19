FROM eclipse-temurin:25-jdk-noble AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN ./gradlew build -x test --no-daemon || return 0

COPY src src
RUN ./gradlew bootJar --no-daemon
RUN java -Djarmode=tools -jar build/libs/*.jar extract --destination build/extracted

FROM eclipse-temurin:25-jre-noble
WORKDIR /app

RUN useradd -m spring
USER spring

COPY --from=builder /app/build/extracted/lib/ ./lib/
COPY --from=builder /app/build/extracted/*.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
