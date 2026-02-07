FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy Maven wrapper and set executable permission
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

# Copy pom.xml and download dependencies offline
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

# Copy source code and build
COPY src src
RUN ./mvnw package -DskipTests

# Copy environment file (optional, if you want .env in container)
COPY .env .env

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/courseshare-0.0.1-SNAPSHOT.jar"]
