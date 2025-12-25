# User Service

Сервис управления пользователями для E-Commerce платформы.

## Функции

- Регистрация пользователей
- Аутентификация (JWT токены)
- Управление профилями пользователей
- Управление ролями и правами доступа

## Технологии

- Spring Boot 4.0
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT (JSON Web Tokens)
- Eureka Client

## API Endpoints

| Метод  | Endpoint               | Описание                    | Авторизация |
|--------|------------------------|-----------------------------|-------------|
| POST   | /api/users/register    | Регистрация пользователя    | Нет         |
| POST   | /api/users/login       | Вход в систему              | Нет         |
| GET    | /api/users/me          | Текущий пользователь        | JWT         |
| GET    | /api/users/{id}        | Получить профиль            | JWT         |
| GET    | /api/users             | Список пользователей        | ADMIN       |
| PUT    | /api/users/{id}        | Обновить профиль            | JWT         |
| DELETE | /api/users/{id}        | Удалить пользователя        | JWT         |

## Примеры запросов

### Регистрация
```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Вход
```bash
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password123"
  }'
```

### Получить текущего пользователя
```bash
curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer <token>"
```

## Тестовые пользователи

При запуске автоматически создаются:

| Username | Password   | Роль  |
|----------|------------|-------|
| admin    | admin123   | ADMIN |
| user     | user123    | USER  |

## Конфигурация

Основные настройки в `application.yml`:

- Порт: 8081
- База данных: PostgreSQL (порт 5432)
- Eureka Server: http://localhost:8761/eureka/
- Config Server: http://localhost:8888

## Запуск

### Локально
```bash
./gradlew :user-service:bootRun
```

### Docker
```bash
docker build -t user-service -f user-service/Dockerfile .
docker run -p 8081:8081 user-service
```

