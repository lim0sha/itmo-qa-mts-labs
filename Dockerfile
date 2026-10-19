FROM maven:3.9.9-eclipse-temurin-21

WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src

ENTRYPOINT ["mvn", "-B", "test"]
