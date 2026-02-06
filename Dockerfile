# Use official OpenJDK image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the jar
RUN ./mvnw package -DskipTests

# Expose port (Render uses 8080)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java","-jar","target/courseshare-0.0.1-SNAPSHOT.jar"]
