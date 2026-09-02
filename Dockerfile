# Multi-stage Dockerfile for Spring Boot + Gradle
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy gradle wrapper and config files
COPY gradle gradle
COPY gradlew build.gradle settings.gradle gradle.properties ./
COPY engine engine
COPY app app

# Grant execution rights on gradlew script and build
RUN chmod +x gradlew
RUN ./gradlew app:bootJar --no-daemon

# Runtime Stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/app/build/libs/*.jar app.jar

ENV PORT=8086
EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]
