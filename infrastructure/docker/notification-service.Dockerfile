# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY config config
COPY build.gradle settings.gradle ./
COPY services services
COPY shared shared

# Normalize line endings (gradlew may be checked out with CRLF on Windows) and make it executable
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Build the notification-service
RUN ./gradlew :services:notification-service:build -x test --no-daemon

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/services/notification-service/build/libs/notification-service-*-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
