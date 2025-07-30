# О проекте
Данный проект представляет собой API для мобильного приложения SoundHub.

# Развертывание проекта

### Переменные окружения

Ниже представлен пример переменных окружения сервиса.

Важно отметить, что в качестве хоста базы данных, redis, kafka служат названия соответствующих сервисов из конфигурации Docker Compose основного проекта.
Для локальной разработки и тестирования используется `localhost`

Основной конфигурационный файл приложения Spring Boot. Здесь необходимо уделить внимание переменной host.url - она определяет полный url медиа-файлов.
Также можно заметить конфигурацию flyway - библиотека, обеспечивающая миграцию базы данных. Flyway не указан в `pom.xml`, при желании его можно добавить и настроить по-своему.

Примечание: в качестве S3 хранилища использовался сервис cloud.ru, поэтому переменные s3 могут немного отличаться от AWS S3.
```yaml
host:
	url: http://192.168.3.5:8080
spring:
	flyway:
		enabled: 'false'
		schemas: migrations
		baselineOnMigrate: 'true'
		locations: classpath:db/migration
		url: jdbc:postgresql://db:5432/soundhub
		user: postgres
		password: postgres
	kafka:
		# docker
		# bootstrap-servers: kafka:9092
		bootstrap-servers: localhost:9092
		error-topic: error
		recommendation:
			group: 'group.user.recommendation'
			request-topic: 'request.user.recommendation'
			response-topic: 'response.user.recommendation'
	jpa:
		hibernate:
			ddl-auto: update
	datasource:
		driver-class-name: org.postgresql.Driver
		password: postgres
		username: postgres
		# docker
		# url: jdbc:postgresql://db:5432/soundhub
		url: jdbc:postgresql://localhost:5432/soundhub
	cache:
		type: redis
	data:
		redis:
			# docker
			# host: redis
			host: localhost
			port: 6379
	servlet:
		multipart:
			max-file-size: 20MB
			max-request-size: 20MB
	web:
		resources:
			add-mappings: false
refreshToken:
	expirationInMs: 604800000
s3:
	region: region
	key:
		id: id
		secret: secret
	endpoint: https://s3.bucket.com
	bucket:
		tenantId: tenantId
		name: bucketName
logging:
	level:
		com:
			soundhub:
				api: DEBUG
		org:
			hibernate:
				transaction: DEBUG
			springframework:
				transaction: DEBUG
media:
	source: s3
	folder:
		posts: posts/
		genres: genres/
		avatars: avatars/
		static: static/
project:
	#    Production
	#    resources:
	#        path: resources
	resources:
		path: src/main/resources
token:
	signing:
		expirationInMs: 3600000
#		HS256
		key: key
```

`env` файл, лежащий в папке `docker`. Необходим для проброса переменных БД в сервис базы данных.
```dotenv
POSTGRES_HOST=host
POSTGRES_PORT=5432
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=soundhub
```


Деплой проекта осуществляется с помощью Docker. На хост-машине необходимо выполнить следующие команды:

1. Сборка и запуск Docker контейнера
`make up`

* [API рекомендательной системы](https://github.com/Pr0gger1/soundhub-recommendation-api)

* [Мобильный Frontend](https://github.com/Pr0gger1/soundhub-app)
