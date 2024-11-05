FROM python:3.12-slim

RUN apt-get update && apt-get install -y openjdk-17-jdk redis-tools

RUN pip install pipenv

WORKDIR /app

COPY target/api-*.jar /app/app.jar

COPY recommendationApi /app/recommendationApi

COPY src/main/resources/application.properties /app/resources/application.properties
COPY src/main/resources/db/ /app/resources/db/

WORKDIR /app/recommendationApi
RUN pipenv install --system --deploy

WORKDIR /app

COPY docker/.env /app/.env
COPY docker/.env /app/recommendationApi/.env

COPY docker/start_server.sh /app/start_server.sh
RUN chmod +x /app/start_server.sh

EXPOSE 8080 8888
ENTRYPOINT ["/app/start_server.sh"]