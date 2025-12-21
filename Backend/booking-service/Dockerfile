# Optimized Dockerfile - uses pre-built JAR
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/booking-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
