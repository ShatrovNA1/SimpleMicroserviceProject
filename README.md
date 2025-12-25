# 🚀 E-Commerce Microservices - Инструкция по запуску

## 📋 Содержание
- [Требования](#требования)
- [Быстрый старт](#быстрый-старт)
- [Запуск в Docker](#запуск-в-docker)
- [Локальная разработка](#локальная-разработка)
- [Доступные сервисы](#доступные-сервисы)
- [API Endpoints](#api-endpoints)
- [Мониторинг](#мониторинг)
- [Troubleshooting](#troubleshooting)

---

## 📦 Требования

### Для Docker (рекомендуется)
- Docker 24.0+
- Docker Compose 2.20+
- 8 GB RAM минимум (рекомендуется 16 GB)
- 20 GB свободного места на диске

### Для локальной разработки
- JDK 21+
- Gradle 8.5+
- PostgreSQL 16 или Docker для баз данных
- Apache Kafka или Docker

---

## ⚡ Быстрый старт

### Вариант 1: Полный запуск в Docker (все сервисы)

```bash
# Клонируйте репозиторий
cd SimpleMicroserviceProject

# Соберите и запустите все сервисы
docker-compose up --build -d

# Подождите ~3-5 минут для запуска всех сервисов
# Проверьте статус
docker-compose ps
```

### Вариант 2: Только инфраструктура + локальные сервисы

```bash
# Запустите только инфраструктуру (PostgreSQL, Kafka, Zipkin, Mailhog)
docker-compose -f docker-compose.infra.yml up -d

# Соберите проект
./gradlew build -x test

# Запустите сервисы в нужном порядке (в отдельных терминалах):
./gradlew :eureka-server:bootRun
./gradlew :config-server:bootRun
./gradlew :api-gateway:bootRun
./gradlew :user-service:bootRun
./gradlew :product-service:bootRun
./gradlew :order-service:bootRun
./gradlew :payment-service:bootRun
./gradlew :notification-service:bootRun
```

---

## 🐳 Запуск в Docker

### Полный запуск

```bash
# Сборка и запуск всех сервисов
docker-compose up --build -d

# Просмотр логов
docker-compose logs -f

# Просмотр логов конкретного сервиса
docker-compose logs -f user-service

# Остановка всех сервисов
docker-compose down

# Остановка с удалением volumes (очистка данных)
docker-compose down -v
```

### Запуск отдельных сервисов

```bash
# Запуск только инфраструктуры
docker-compose -f docker-compose.infra.yml up -d

# Сборка конкретного сервиса
docker-compose build user-service

# Запуск конкретного сервиса
docker-compose up -d user-service
```

### Пересборка после изменений

```bash
# Пересборка одного сервиса
docker-compose build --no-cache user-service
docker-compose up -d user-service

# Пересборка всех сервисов
docker-compose build --no-cache
docker-compose up -d
```

---

## 💻 Локальная разработка

### Шаг 1: Запустите инфраструктуру

```bash
docker-compose -f docker-compose.infra.yml up -d
```

### Шаг 2: Соберите проект

```bash
./gradlew clean build -x test
```

### Шаг 3: Запустите сервисы по порядку

**Важно!** Сервисы должны запускаться в определённом порядке:

1. **Eureka Server** (Service Discovery)
   ```bash
   ./gradlew :eureka-server:bootRun
   ```
   Подождите пока запустится (http://localhost:8761)

2. **Config Server** (Configuration)
   ```bash
   ./gradlew :config-server:bootRun
   ```
   Подождите пока запустится (http://localhost:8888/actuator/health)

3. **API Gateway**
   ```bash
   ./gradlew :api-gateway:bootRun
   ```

4. **Business Services** (можно запускать параллельно)
   ```bash
   ./gradlew :user-service:bootRun
   ./gradlew :product-service:bootRun
   ./gradlew :order-service:bootRun
   ./gradlew :payment-service:bootRun
   ./gradlew :notification-service:bootRun
   ```

---

## 🌐 Доступные сервисы

| Сервис | URL | Описание |
|--------|-----|----------|
| **API Gateway** | http://localhost:8080 | Единая точка входа |
| **Eureka Dashboard** | http://localhost:8761 | Service Discovery UI |
| **Config Server** | http://localhost:8888 | Centralized Configuration |
| **User Service** | http://localhost:8081 | Управление пользователями |
| **Product Service** | http://localhost:8082 | Каталог товаров |
| **Order Service** | http://localhost:8083 | Управление заказами |
| **Payment Service** | http://localhost:8084 | Обработка платежей |
| **Notification Service** | http://localhost:8085 | Уведомления |
| **Zipkin** | http://localhost:9411 | Distributed Tracing |
| **Mailhog** | http://localhost:8025 | Email Testing UI |

---

## 🔌 API Endpoints

### Через API Gateway (рекомендуется)

```bash
# User Service
curl http://localhost:8080/api/users
curl http://localhost:8080/api/users/1

# Product Service
curl http://localhost:8080/api/products
curl http://localhost:8080/api/categories

# Order Service
curl http://localhost:8080/api/orders
curl http://localhost:8080/api/orders/1

# Payment Service
curl http://localhost:8080/api/payments
curl http://localhost:8080/api/payments/1

# Notification Service
curl http://localhost:8080/api/notifications
curl http://localhost:8080/api/notifications/stats
```

### Примеры запросов

#### Создание пользователя
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+79001234567"
  }'
```

#### Создание товара
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 15",
    "description": "Latest Apple smartphone",
    "price": 999.99,
    "stockQuantity": 100,
    "sku": "IPHONE-15-001"
  }'
```

#### Создание заказа
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ],
    "shippingAddress": "123 Main St, City, Country"
  }'
```

---

## 📊 Мониторинг

### Health Checks

```bash
# Проверка всех сервисов
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
```

### Eureka Dashboard

Откройте http://localhost:8761 для просмотра зарегистрированных сервисов.

### Zipkin Tracing

Откройте http://localhost:9411 для просмотра распределённых трейсов.

### Mailhog

Откройте http://localhost:8025 для просмотра отправленных email.

---

## 🔧 Troubleshooting

### Проблема: Сервис не запускается

1. Проверьте что Eureka Server запущен:
   ```bash
   curl http://localhost:8761/actuator/health
   ```

2. Проверьте логи сервиса:
   ```bash
   docker-compose logs -f <service-name>
   # или
   ./gradlew :<service-name>:bootRun --console=plain
   ```

### Проблема: Connection refused к базе данных

1. Проверьте что PostgreSQL запущен:
   ```bash
   docker-compose ps | grep postgres
   ```

2. Проверьте подключение:
   ```bash
   docker exec -it postgres-user psql -U postgres -d userdb -c "SELECT 1"
   ```

### Проблема: Kafka не работает

1. Проверьте статус Kafka и Zookeeper:
   ```bash
   docker-compose ps | grep -E "kafka|zookeeper"
   ```

2. Проверьте топики:
   ```bash
   docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list
   ```

### Проблема: Не хватает памяти

Добавьте в docker-compose лимиты памяти:
```yaml
services:
  user-service:
    deploy:
      resources:
        limits:
          memory: 512M
```

### Очистка Docker

```bash
# Остановить все контейнеры
docker-compose down

# Удалить все volumes
docker-compose down -v

# Очистить неиспользуемые образы
docker system prune -a

# Очистить всё (осторожно!)
docker system prune -a --volumes
```

---

## 📁 Структура проекта

```
SimpleMicroserviceProject/
├── api-gateway/          # API Gateway (Spring Cloud Gateway)
├── config-server/        # Config Server (Spring Cloud Config)
├── config-repo/          # Git repo для конфигураций
├── eureka-server/        # Service Discovery (Netflix Eureka)
├── user-service/         # Управление пользователями
├── product-service/      # Каталог товаров
├── order-service/        # Управление заказами
├── payment-service/      # Обработка платежей
├── notification-service/ # Уведомления (email, SMS)
├── docker/               # Docker configs (Prometheus, Grafana)
├── docs/                 # Документация
├── docker-compose.yml    # Полная конфигурация Docker
└── docker-compose.infra.yml  # Только инфраструктура
```

---

## 🔗 Полезные ссылки

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Docker Documentation](https://docs.docker.com/)
- [Kafka Documentation](https://kafka.apache.org/documentation/)

