FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/jenkins-remoting-project-1.0-SNAPSHOT.jar app.jar
ENV PORT=10000
EXPOSE 10000
CMD ["java", "-cp", "app.jar", "com.example.remoting.WebServer"]
