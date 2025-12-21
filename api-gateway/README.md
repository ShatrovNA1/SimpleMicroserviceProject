# API Gateway

## Описание
API Gateway - единая точка входа для всех микросервисов E-Commerce платформы.

## Порт
- **8080**

## Основные функции
- ✅ Маршрутизация запросов к микросервисам
- ✅ JWT аутентификация и авторизация
- ✅ Rate Limiting (100 запросов/минуту с одного IP)
- ✅ Circuit Breaker (Resilience4j)
- ✅ Логирование запросов
- ✅ CORS конфигурация
- ✅ Service Discovery через Eureka
- ✅ Централизованная конфигурация через Config Server

## Маршруты

| Сервис | Путь | Аутентификация |
|--------|------|----------------|
| User Service | `/api/users/**` | Частичная* |
| Product Service | `/api/products/**`, `/api/categories/**` | GET - нет, остальные - да |
| Order Service | `/api/orders/**` | Да |
| Payment Service | `/api/payments/**` | Да |
| Notification Service | `/api/notifications/**` | Да |

*Открытые endpoints: `/api/users/register`, `/api/users/login`

## Запуск

### Предварительные требования
1. Запущен Eureka Server (порт 8761)
2. Запущен Config Server (порт 8888)

### Команда запуска
```bash
./gradlew :api-gateway:bootRun
```

## Endpoints

### Gateway Info
- `GET /api/gateway/status` - Статус сервиса
- `GET /api/gateway/info` - Информация о сервисе

### Actuator
- `GET /actuator/health` - Health check
- `GET /actuator/info` - Информация
- `GET /actuator/gateway/routes` - Список маршрутов
- `GET /actuator/circuitbreakers` - Статус Circuit Breakers

### Fallback endpoints
При недоступности сервиса возвращается fallback ответ:
```json
{
  "status": 503,
  "error": "Service Unavailable",
  "message": "Service Name is currently unavailable. Please try again later.",
  "timestamp": "2025-01-21T10:00:00",
  "service": "Service Name"
}
```

## Конфигурация

### JWT
Токен передается в заголовке:
```
Authorization: Bearer <jwt_token>
```

При успешной валидации в downstream сервисы передаются заголовки:
- `X-User-Id` - ID пользователя
- `X-Username` - Имя пользователя
- `X-User-Roles` - Роли пользователя (через запятую)

### Rate Limiting
- Лимит: 100 запросов в минуту с одного IP
- При превышении возвращается HTTP 429 (Too Many Requests)

### Circuit Breaker
- Sliding window: 10 запросов
- Failure rate threshold: 50%
- Wait duration in open state: 10 секунд
- Timeout: 5 секунд

## Структура проекта
```
api-gateway/
├── src/main/java/com/ecommerce/gateway/
│   ├── ApiGatewayApplication.java
│   ├── config/
│   │   ├── CorsConfig.java
│   │   └── GatewayConfig.java
│   ├── controller/
│   │   ├── FallbackController.java
│   │   └── GatewayInfoController.java
│   ├── exception/
│   │   └── GlobalExceptionHandler.java
│   ├── filter/
│   │   ├── AuthenticationFilter.java
│   │   ├── LoggingFilter.java
│   │   ├── RateLimitingFilter.java
│   │   └── RouteValidator.java
│   └── util/
│       └── JwtUtil.java
├── src/main/resources/
│   └── application.yml
└── build.gradle.kts
```

