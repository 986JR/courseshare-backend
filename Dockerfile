FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw package -DskipTests

# copy env BEFORE running
COPY .env .env

EXPOSE 8080
ENTRYPOINT ["java","-jar","target/courseshare-0.0.1-SNAPSHOT.jar"]
