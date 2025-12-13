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

# email-service runs on 8085
EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
