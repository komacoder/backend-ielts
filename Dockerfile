# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-23 AS build
WORKDIR /app
# Copy the pom.xml file first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copy the rest of the source code
COPY src ./src
# Package the application (skipping tests for faster builds)
RUN mvn clean package -DskipTests
# Stage 2: Create the runtime environment
FROM eclipse-temurin:23-jre-alpine
WORKDIR /app
# Copy the compiled JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar
# Expose the port your Spring Boot app runs on
EXPOSE 8080
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
