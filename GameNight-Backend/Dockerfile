# Use a Java 21 base image for Spring Boot
FROM openjdk:21-jdk-slim AS build

# Set working directory
WORKDIR /app

# Copy the Gradle wrapper and Gradle files
COPY gradlew gradlew.bat build.gradle settings.gradle /app/
COPY gradle /app/gradle

# Copy the source code
COPY src /app/src

# Run Gradle build (this also downloads the necessary dependencies)
RUN ./gradlew build -x test

# Run the application in a smaller JRE image
FROM openjdk:21-jdk

# Set working directory
WORKDIR /app

# Copy the compiled JAR file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port the app will run on
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
