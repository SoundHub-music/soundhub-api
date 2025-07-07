FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y redis-tools

WORKDIR /app

COPY target/api-*.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "./app.jar"]