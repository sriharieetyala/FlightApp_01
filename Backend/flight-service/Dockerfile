# Optimized Dockerfile - uses pre-built JAR
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/flight-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
