<<<<<<< Updated upstream
<<<<<<< Updated upstream
# Optimized Dockerfile - uses pre-built JAR
=======

>>>>>>> Stashed changes
FROM eclipse-temurin:21-jre
=======

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY target/*.jar app.jar
>>>>>>> Stashed changes

WORKDIR /app
<<<<<<< Updated upstream
COPY target/booking-service-*.jar app.jar
=======
COPY target/*.jar app.jar

>>>>>>> Stashed changes
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
