# Notification Service

Микросервис для отправки уведомлений пользователям (email, SMS, push-уведомления).

## Функциональность

- Отправка email уведомлений
- Отправка SMS уведомлений (симуляция)
- Push-уведомления (заглушка)
- Поддержка Thymeleaf шаблонов для email
- Асинхронная отправка уведомлений
- Автоматическая повторная отправка неудачных уведомлений
- Интеграция с Kafka для получения событий от других сервисов

## API Endpoints

### Уведомления

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/api/notifications` | Отправить уведомление |
| POST | `/api/notifications/order` | Отправить уведомление о заказе |
| GET | `/api/notifications/{id}` | Получить уведомление по ID |
| GET | `/api/notifications/user/{userId}` | Получить уведомления пользователя |
| GET | `/api/notifications/status/{status}` | Получить уведомления по статусу |
| GET | `/api/notifications` | Получить все уведомления |
| POST | `/api/notifications/{id}/retry` | Повторить отправку уведомления |
| POST | `/api/notifications/{id}/cancel` | Отменить уведомление |
| GET | `/api/notifications/stats` | Получить статистику уведомлений |

## Типы уведомлений

- `ORDER_CREATED` - Заказ создан
- `ORDER_CONFIRMED` - Заказ подтверждён
- `ORDER_SHIPPED` - Заказ отправлен
- `ORDER_DELIVERED` - Заказ доставлен
- `ORDER_CANCELLED` - Заказ отменён
- `PAYMENT_SUCCESS` - Оплата успешна
- `PAYMENT_FAILED` - Оплата не удалась
- `PAYMENT_REFUNDED` - Возврат средств
- `WELCOME` - Приветственное сообщение
- `PASSWORD_RESET` - Сброс пароля
- `ACCOUNT_VERIFIED` - Аккаунт подтверждён
- `PROMOTIONAL` - Промо-рассылка

## Каналы доставки

- `EMAIL` - Электронная почта
- `SMS` - SMS сообщения
- `PUSH` - Push-уведомления

## Kafka Topics

Сервис слушает следующие топики:
- `order-events` - События заказов
- `payment-events` - События платежей
- `user-events` - События пользователей

## Конфигурация

```yaml
notification:
  email:
    from: noreply@ecommerce.com
    enabled: true
  sms:
    enabled: false
  retry:
    max-attempts: 3
    interval: 300000  # 5 минут
```

## Технологии

- Spring Boot 3.x
- Spring Data JPA
- Spring Mail
- Thymeleaf
- Apache Kafka
- PostgreSQL
- Eureka Client

## Порт

По умолчанию сервис работает на порту `8085`

