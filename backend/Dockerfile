# 1. Mərhələ: Layihəni yığmaq (Build)
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Mərhələ: Layihəni işə salmaq (Run)
# Burada hamı tərəfindən qəbul edilən rəsmi Eclipse Temurin obrazından istifadə edirik
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]