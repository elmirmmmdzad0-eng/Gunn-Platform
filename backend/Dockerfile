# 1. Mərhələ: Layihəni yığmaq (Build)
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
# Maven-ə deyirik ki, 'backend' qovluğunun içinə girib pom.xml-i ordan oxusun
RUN mvn -f backend/pom.xml clean package -DskipTests

# 2. Mərhələ: Layihəni işə salmaq (Run)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# Hazır olan jar faylını backend-in hədəf qovluğundan bura köçürürük
COPY --from=build /app/backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]