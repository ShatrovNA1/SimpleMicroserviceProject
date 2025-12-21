# Config Server

## Описание
Config Server предоставляет централизованное управление конфигурациями для всех микросервисов.

## Порт
- **8888**

## Технологии
- Spring Boot 4.0.1
- Spring Cloud Config Server 2025.1.0
- Spring Cloud Netflix Eureka Client
- Java 25

## Функции
- ✅ Централизованное хранение конфигураций
- ✅ Native profile (файловая система)
- ✅ Регистрация в Eureka
- ✅ Динамическое обновление конфигураций

## Конфигурационные файлы

Все конфигурации хранятся в директории `config-repo/`:

| Файл | Описание |
|------|----------|
| `application.yml` | Общие настройки для всех сервисов |
| `api-gateway.yml` | Настройки API Gateway |
| `user-service.yml` | Настройки User Service |
| `product-service.yml` | Настройки Product Service |
| `order-service.yml` | Настройки Order Service |
| `payment-service.yml` | Настройки Payment Service |
| `notification-service.yml` | Настройки Notification Service |

## Запуск

### Предварительные требования
- Запущен Eureka Server на порту 8761

### Сборка
```bash
./gradlew :config-server:build
```

### Запуск
```bash
./gradlew :config-server:bootRun
```

## API Endpoints

### Получение конфигурации
```bash
# Общая конфигурация
curl http://localhost:8888/application/default

# Конфигурация конкретного сервиса
curl http://localhost:8888/user-service/default
curl http://localhost:8888/product-service/default
curl http://localhost:8888/order-service/default
```

### Health Check
```bash
curl http://localhost:8888/actuator/health
```

## Как сервисы получают конфигурацию

Добавьте в сервис зависимость:
```kotlin
implementation("org.springframework.cloud:spring-cloud-starter-config")
```

И в `application.yml`:
```yaml
spring:
  config:
    import: optional:configserver:http://localhost:8888
  application:
    name: your-service-name
```

## Структура
```
config-server/
├── build.gradle.kts
├── README.md
└── src/
    ├── main/
    │   ├── java/com/ecommerce/config/
    │   │   └── ConfigServerApplication.java
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/com/ecommerce/config/
            └── ConfigServerApplicationTests.java

config-repo/
├── application.yml
├── api-gateway.yml
├── user-service.yml
├── product-service.yml
├── order-service.yml
├── payment-service.yml
└── notification-service.yml
```

