
# Stage 1: Build the Java application
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven configuration and source code
COPY pom.xml .
COPY src ./src

# Compile, run tests and package the application
RUN mvn -B clean package

# Stage 2: Run the application
FROM eclipse-temurin:21-jre

WORKDIR /app

# Create a non-root user for better security
RUN useradd --system --uid 10001 appuser

# Copy the executable JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Run as a non-root user
USER 10001

# Document the application port
EXPOSE 8082

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
