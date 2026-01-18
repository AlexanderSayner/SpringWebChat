# Use official OpenJDK runtime as base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Gradle wrapper files
COPY gradlew .
COPY gradle/ ./gradle/

# Make the gradlew executable
RUN chmod +x ./gradlew

# Copy the build.gradle file
COPY build.gradle .

# Download dependencies
RUN ./gradlew build --exclude-task=:bootJar --exclude-task=:jar --console=plain

# Copy the rest of the application source code
COPY src/ ./src/

# Build the application
RUN ./gradlew bootJar --console=plain

# Expose port 8080
EXPOSE 8080

# Define environment variables with defaults
ENV SERVER_PORT=8080
ENV DATABASE_URL=jdbc:postgresql://localhost:5432/chatdb
ENV DATABASE_USERNAME=postgres
ENV DATABASE_PASSWORD=password
ENV VK_CLIENT_ID=your-vk-app-id
ENV VK_CLIENT_SECRET=your-vk-app-secret
ENV VK_REDIRECT_URI=http://localhost:8080/login/oauth2/code/vk

# Run the application
ENTRYPOINT ["java", "-jar", "build/libs/chat-0.0.1-SNAPSHOT.jar"]