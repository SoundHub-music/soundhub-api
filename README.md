# О проекте
Данный проект представляет собой API для мобильного приложения SoundHub.

# Развертывание проекта

## Переменные окружения

Важно отметить, что в качестве хоста базы данных, redis, kafka служат названия соответствующих сервисов из конфигурации Docker Compose основного проекта.
Для локальной разработки и тестирования используется `localhost`

Основной конфигурационный файл приложения Spring Boot. Здесь необходимо уделить внимание переменной host.url - она определяет полный url медиа-файлов.
Также можно заметить конфигурацию flyway - библиотека, обеспечивающая миграцию базы данных. Flyway не указан в `pom.xml`, при желании его можно добавить и настроить по-своему.

Примечание: в качестве S3 хранилища использовался сервис cloud.ru, поэтому переменные s3 могут немного отличаться от AWS S3.
```yaml
host:
	url: url
spring:
	flyway:
		enabled: 'false'
		schemas: migrations
		baselineOnMigrate: 'true'
		locations: classpath:db/migration
		url: database-url
		user: database-user
		password: database-password
	jpa:
		hibernate:
			ddl-auto: update
	datasource:
		driver-class-name: database-driver # org.postgresql.Driver
		password: database-password
		username: database-user
		url: database-url # jdbc:postgresql://localhost:5432/dbname
	cache:
		type: redis
	data:
		redis:
			host: redis # localhost
			port: '6379'
	servlet:
		multipart:
			max-file-size: 20MB
			max-request-size: 20MB
	web:
		resources:
			add-mappings: 'false'
refreshToken:
	expirationInMs: '604800000'
s3: # config for cloud.ru
	region: region
	key:
		id: key-id
		secret: key-secret
	endpoint: endpoint
	bucket:
		tenantId: tenant-id
		name: bucket-name
recommendation:
	url: url
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
	source: s3 # or local
	folder:
		posts: posts/
		genres: genres/
		avatars: avatars/
		static: static/
project:
	#    Production
	resources:
		path: resources
	#    Development
#    resources:
#        path: src/main/resources
token:
	signing:
		expirationInMs: '3600000'
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

## Сборка и запуск проекта

Деплой проекта осуществляется с помощью Docker. На хост-машине необходимо выполнить команду `make up`

Со всеми командами Makefile можно ознакомиться с помощью `make help`

# Ссылки

* [API рекомендательной системы](https://github.com/Pr0gger1/soundhub-recommendation-api)

* [Мобильный Frontend](https://github.com/Pr0gger1/soundhub-app)
