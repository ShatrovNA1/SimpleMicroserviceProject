# Order Service

Сервис управления заказами для E-Commerce платформы.

## Функции

- Создание заказов с автоматическим резервированием товаров
- Управление статусами заказов (workflow)
- Интеграция с Product Service (резервирование/освобождение товаров)
- Интеграция с Payment Service (оплата/возврат)
- Circuit Breaker для отказоустойчивости

## Технологии

- Spring Boot 4.0
- Spring Data JPA
- Spring Cloud OpenFeign
- Resilience4j Circuit Breaker
- PostgreSQL
- Eureka Client

## Статусы заказа

```
PENDING → PAID → PROCESSING → SHIPPED → DELIVERED
    ↓       ↓         ↓
CANCELLED  REFUNDED  CANCELLED
```

## API Endpoints

| Метод  | Endpoint                    | Описание                     |
|--------|-----------------------------|------------------------------|
| POST   | /api/orders                 | Создать заказ                |
| GET    | /api/orders/{id}            | Получить заказ по ID         |
| GET    | /api/orders/number/{number} | Получить заказ по номеру     |
| GET    | /api/orders/user/{userId}   | Заказы пользователя          |
| GET    | /api/orders/status/{status} | Заказы по статусу            |
| GET    | /api/orders                 | Все заказы (пагинация)       |
| PUT    | /api/orders/{id}/status     | Обновить статус              |
| POST   | /api/orders/{id}/pay        | Оплатить заказ               |
| POST   | /api/orders/{id}/cancel     | Отменить заказ               |

## Примеры запросов

### Создать заказ
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {"productId": 1, "quantity": 2},
      {"productId": 2, "quantity": 1}
    ],
    "shippingAddress": "123 Main St, City, Country",
    "notes": "Please deliver in the morning"
  }'
```

### Оплатить заказ
```bash
curl -X POST "http://localhost:8083/api/orders/1/pay?paymentMethod=CREDIT_CARD"
```

### Отменить заказ
```bash
curl -X POST http://localhost:8083/api/orders/1/cancel
```

### Обновить статус
```bash
curl -X PUT http://localhost:8083/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "SHIPPED"}'
```

## Конфигурация

- Порт: 8083
- База данных: PostgreSQL (порт 5434)
- Eureka Server: http://localhost:8761/eureka/

## Circuit Breaker

Настройки Resilience4j:
- slidingWindowSize: 10
- failureRateThreshold: 50%
- waitDurationInOpenState: 10s

## Запуск

### Локально
```bash
./gradlew :order-service:bootRun
```

### Docker
```bash
docker build -t order-service -f order-service/Dockerfile .
docker run -p 8083:8083 order-service
```

