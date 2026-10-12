FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY backend/pom.xml backend/pom.xml
COPY backend/src backend/src
COPY frontend frontend

WORKDIR /workspace/backend
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app/backend
RUN mkdir -p data file-vault
COPY --from=build /workspace/backend/target/backend-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
