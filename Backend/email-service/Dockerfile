# Optimized Dockerfile - uses pre-built JAR
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/email-service-*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
