# 1. Mərhələ: Layihəni yığmaq (Build) - Java 21 istifadə edirik
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -f backend/pom.xml clean package -DskipTests

# 2. Mərhələ: Layihəni işə salmaq (Run) - Java 21 Runtime
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]