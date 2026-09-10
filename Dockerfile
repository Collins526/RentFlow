FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml ./pom.xml
COPY backend/pom.xml ./backend/pom.xml
RUN mvn -f backend/pom.xml dependency:go-offline -DskipTests
COPY backend/src ./backend/src
RUN mvn -f backend/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/backend/target/rentflow-backend-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
