# ===== BUILD STAGE =====
FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package spring-boot:repackage -Dmaven.test.skip=true



# ===== RUNTIME STAGE =====
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

# Eureka Server runs on 8761
EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
