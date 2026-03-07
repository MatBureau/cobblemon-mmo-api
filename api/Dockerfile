# Stage 1: Build
FROM gradle:8-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle :api:shadowJar --no-daemon

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/api/build/libs/api-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
