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
COPY target/api-gateway-*.jar app.jar
=======
COPY target/*.jar app.jar

>>>>>>> Stashed changes
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
