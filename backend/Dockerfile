FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

EXPOSE 8080

# Using the exact JAR name for a stable deployment
CMD ["java", "-jar", "target/faculty-load-analyzer-1.0.0.jar"]