# Multi-stage build for Spring Boot application
# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for health check (optional, Render.com handles health checks)
RUN apk add --no-cache curl

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create uploads directory with proper permissions
RUN mkdir -p /app/uploads && chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose port (Render.com will set PORT env var)
EXPOSE 8080

# Health check (Render.com will handle this, but we include it for local testing)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/api/users/login || exit 1

# Run the application
# Render.com sets PORT env var, Spring Boot will read it from application-production.yml
# Set profile to production for Render.com deployment
ENTRYPOINT ["sh", "-c", "java -jar -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev} -Djava.security.egd=file:/dev/./urandom app.jar"]

