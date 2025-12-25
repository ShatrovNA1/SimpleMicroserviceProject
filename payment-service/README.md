# Payment Service

Сервис обработки платежей для E-Commerce платформы.

## Функции

- Обработка платежей (имитация платёжного шлюза)
- Поддержка разных методов оплаты
- Полный возврат средств (refund)
- Частичный возврат
- История платежей и транзакций

## Технологии

- Spring Boot 4.0
- Spring Data JPA
- PostgreSQL
- Eureka Client

## Методы оплаты

- `CREDIT_CARD` - кредитная карта
- `DEBIT_CARD` - дебетовая карта
- `PAYPAL` - PayPal
- `BANK_TRANSFER` - банковский перевод
- `CRYPTO` - криптовалюта

## Статусы платежа

```
PENDING → PROCESSING → COMPLETED → REFUNDED
              ↓
            FAILED

PENDING/PROCESSING → CANCELLED
```

## API Endpoints

| Метод  | Endpoint                          | Описание                       |
|--------|-----------------------------------|--------------------------------|
| POST   | /api/payments                     | Обработать платёж              |
| GET    | /api/payments/{id}                | Получить платёж по ID          |
| GET    | /api/payments/order/{orderId}     | Получить платёж по заказу      |
| GET    | /api/payments/transaction/{txnId} | Получить по ID транзакции      |
| GET    | /api/payments/user/{userId}       | Платежи пользователя           |
| GET    | /api/payments/status/{status}     | Платежи по статусу             |
| GET    | /api/payments                     | Все платежи (пагинация)        |
| POST   | /api/payments/{id}/refund         | Возврат средств                |
| GET    | /api/payments/{id}/refunds        | История возвратов              |
| POST   | /api/payments/{id}/cancel         | Отменить платёж                |

## Примеры запросов

### Обработать платёж
```bash
curl -X POST http://localhost:8084/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "orderNumber": "ORD-20251225-ABC123",
    "userId": 1,
    "amount": 99.99,
    "paymentMethod": "CREDIT_CARD"
  }'
```

### Возврат средств (полный)
```bash
curl -X POST http://localhost:8084/api/payments/1/refund
```

### Частичный возврат
```bash
curl -X POST http://localhost:8084/api/payments/1/refund \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 50.00,
    "reason": "Partial refund for returned item"
  }'
```

### Получить платёж по заказу
```bash
curl http://localhost:8084/api/payments/order/1
```

## Симуляция платёжного шлюза

Сервис использует симулятор платёжного шлюза с настраиваемыми параметрами:

```yaml
payment:
  simulation:
    success-rate: 0.95  # 95% успешных платежей
    processing-delay: 1000  # Задержка 1 секунда
```

В продакшене здесь была бы интеграция с реальным платёжным провайдером (Stripe, PayPal, etc.)

## Конфигурация

- Порт: 8084
- База данных: PostgreSQL (порт 5435)
- Eureka Server: http://localhost:8761/eureka/

## Запуск

### Локально
```bash
./gradlew :payment-service:bootRun
```

### Docker
```bash
docker build -t payment-service -f payment-service/Dockerfile .
docker run -p 8084:8084 payment-service
```

