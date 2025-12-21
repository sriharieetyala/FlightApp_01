# Optimized Dockerfile - uses pre-built JAR
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/auth-service-*.jar app.jar
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]
