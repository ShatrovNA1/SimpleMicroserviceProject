# Техническое задание: Система управления интернет-магазином

## 1. Общее описание проекта

**Название:** E-Commerce Microservices Platform

**Цель:** Создание учебного проекта на основе микросервисной архитектуры для демонстрации современных подходов к разработке распределенных систем.

**Уровень сложности:** Средний (подходит для обучения)

---

## 2. Бизнес-требования

### 2.1 Описание системы
Система представляет собой упрощенный интернет-магазин, позволяющий:
- Управлять каталогом товаров
- Оформлять заказы
- Управлять пользователями и их профилями
- Обрабатывать платежи (имитация)
- Отправлять уведомления

### 2.2 Основные функции
1. **Управление товарами**: CRUD операции с товарами
2. **Управление пользователями**: регистрация, аутентификация, профили
3. **Корзина покупок**: добавление/удаление товаров
4. **Заказы**: создание, обработка, отслеживание статуса
5. **Платежи**: имитация обработки платежей
6. **Уведомления**: email/SMS уведомления о статусе заказа

---

## 3. Архитектура системы

### 3.1 Микросервисы

#### 3.1.1 API Gateway
- **Порт:** 8080
- **Технологии:** Spring Cloud Gateway
- **Функции:**
  - Единая точка входа для всех запросов
  - Маршрутизация запросов к соответствующим сервисам
  - Аутентификация и авторизация (JWT)
  - Rate limiting
  - Логирование запросов

#### 3.1.2 Service Registry (Eureka Server)
- **Порт:** 8761
- **Технологии:** Spring Cloud Netflix Eureka
- **Функции:**
  - Регистрация всех микросервисов
  - Service discovery
  - Health checking

#### 3.1.3 Config Server
- **Порт:** 8888
- **Технологии:** Spring Cloud Config
- **Функции:**
  - Централизованное управление конфигурациями
  - Хранение конфигураций в Git репозитории
  - Динамическое обновление конфигураций

#### 3.1.4 User Service
- **Порт:** 8081
- **База данных:** PostgreSQL (порт 5432)
- **Технологии:** Spring Boot, Spring Data JPA, Spring Security
- **Функции:**
  - Регистрация пользователей
  - Аутентификация (выдача JWT токенов)
  - Управление профилями пользователей
  - Управление ролями и правами доступа

**API endpoints:**
```
POST   /api/users/register       - Регистрация
POST   /api/users/login          - Вход
GET    /api/users/{id}           - Получить профиль
PUT    /api/users/{id}           - Обновить профиль
DELETE /api/users/{id}           - Удалить пользователя
GET    /api/users                - Список пользователей (admin)
```

**Модель данных:**
```
User:
  - id (Long)
  - username (String)
  - email (String)
  - password (String, bcrypt)
  - firstName (String)
  - lastName (String)
  - roles (Set<Role>)
  - createdAt (LocalDateTime)
  - updatedAt (LocalDateTime)
```

#### 3.1.5 Product Service
- **Порт:** 8082
- **База данных:** PostgreSQL (порт 5433)
- **Технологии:** Spring Boot, Spring Data JPA
- **Функции:**
  - CRUD операции с товарами
  - Управление категориями
  - Управление запасами (inventory)
  - Поиск и фильтрация товаров

**API endpoints:**
```
GET    /api/products              - Список товаров (пагинация, фильтры)
GET    /api/products/{id}         - Получить товар
POST   /api/products              - Создать товар (admin)
PUT    /api/products/{id}         - Обновить товар (admin)
DELETE /api/products/{id}         - Удалить товар (admin)
GET    /api/products/search       - Поиск товаров
GET    /api/categories            - Список категорий
POST   /api/products/{id}/stock   - Обновить количество на складе
```

**Модель данных:**
```
Product:
  - id (Long)
  - name (String)
  - description (String)
  - price (BigDecimal)
  - categoryId (Long)
  - stockQuantity (Integer)
  - imageUrl (String)
  - createdAt (LocalDateTime)
  - updatedAt (LocalDateTime)

Category:
  - id (Long)
  - name (String)
  - description (String)
```

#### 3.1.6 Order Service
- **Порт:** 8083
- **База данных:** PostgreSQL (порт 5434)
- **Технологии:** Spring Boot, Spring Data JPA, Spring Cloud Stream
- **Функции:**
  - Создание заказов
  - Управление статусами заказов
  - История заказов пользователя
  - Интеграция с Product Service (проверка наличия)
  - Интеграция с Payment Service
  - Публикация событий при изменении статуса

**API endpoints:**
```
POST   /api/orders                - Создать заказ
GET    /api/orders/{id}           - Получить заказ
GET    /api/orders/user/{userId}  - Заказы пользователя
PUT    /api/orders/{id}/status    - Обновить статус (admin)
DELETE /api/orders/{id}           - Отменить заказ
```

**Модель данных:**
```
Order:
  - id (Long)
  - userId (Long)
  - orderNumber (String)
  - status (OrderStatus: PENDING, CONFIRMED, PAID, SHIPPED, DELIVERED, CANCELLED)
  - totalAmount (BigDecimal)
  - items (List<OrderItem>)
  - shippingAddress (String)
  - createdAt (LocalDateTime)
  - updatedAt (LocalDateTime)

OrderItem:
  - id (Long)
  - orderId (Long)
  - productId (Long)
  - productName (String)
  - quantity (Integer)
  - price (BigDecimal)
```

#### 3.1.7 Payment Service
- **Порт:** 8084
- **База данных:** PostgreSQL (порт 5435)
- **Технологии:** Spring Boot, Spring Data JPA
- **Функции:**
  - Имитация обработки платежей
  - История платежей
  - Возврат средств (refund)
  - Интеграция с Order Service

**API endpoints:**
```
POST   /api/payments              - Обработать платеж
GET    /api/payments/{id}         - Получить информацию о платеже
GET    /api/payments/order/{orderId} - Платежи по заказу
POST   /api/payments/{id}/refund  - Возврат средств
```

**Модель данных:**
```
Payment:
  - id (Long)
  - orderId (Long)
  - userId (Long)
  - amount (BigDecimal)
  - paymentMethod (PaymentMethod: CREDIT_CARD, DEBIT_CARD, PAYPAL)
  - status (PaymentStatus: PENDING, SUCCESS, FAILED, REFUNDED)
  - transactionId (String)
  - createdAt (LocalDateTime)
  - updatedAt (LocalDateTime)
```

#### 3.1.8 Notification Service
- **Порт:** 8085
- **База данных:** MongoDB (порт 27017)
- **Технологии:** Spring Boot, Spring Data MongoDB, Spring Cloud Stream
- **Функции:**
  - Отправка email уведомлений (имитация)
  - Отправка SMS (имитация)
  - История уведомлений
  - Подписка на события из других сервисов

**API endpoints:**
```
POST   /api/notifications/send    - Отправить уведомление
GET    /api/notifications/user/{userId} - Уведомления пользователя
```

**Модель данных:**
```
Notification:
  - id (String)
  - userId (Long)
  - type (NotificationType: EMAIL, SMS, PUSH)
  - subject (String)
  - message (String)
  - status (NotificationStatus: PENDING, SENT, FAILED)
  - sentAt (LocalDateTime)
  - createdAt (LocalDateTime)
```

### 3.2 Инфраструктурные компоненты

#### 3.2.1 Message Broker (RabbitMQ или Kafka)
- **Порт:** 5672 (RabbitMQ) или 9092 (Kafka)
- **Функции:**
  - Асинхронная коммуникация между сервисами
  - Event-driven архитектура
  - Гарантия доставки сообщений

**События:**
- `OrderCreatedEvent` - заказ создан
- `OrderStatusChangedEvent` - статус заказа изменен
- `PaymentProcessedEvent` - платеж обработан
- `StockUpdatedEvent` - запасы обновлены

#### 3.2.2 Distributed Tracing (Zipkin)
- **Порт:** 9411
- **Функции:**
  - Трассировка запросов через микросервисы
  - Визуализация цепочек вызовов
  - Анализ производительности

#### 3.2.3 Monitoring (Prometheus + Grafana)
- **Prometheus порт:** 9090
- **Grafana порт:** 3000
- **Функции:**
  - Сбор метрик
  - Визуализация метрик
  - Alerting

---

## 4. Технологический стек

### 4.1 Backend
- **Java:** 17+
- **Spring Boot:** 3.2.x
- **Spring Cloud:** 2023.0.x
- **Spring Data JPA:** для работы с PostgreSQL
- **Spring Security:** аутентификация и авторизация
- **JWT:** токены доступа
- **Lombok:** уменьшение boilerplate кода
- **MapStruct:** маппинг DTO
- **Gradle:** система сборки

### 4.2 Базы данных
- **PostgreSQL:** для всех сервисов (User, Product, Order, Payment, Notification)

### 4.3 Message Broker
- **RabbitMQ** (рекомендуется для начинающих) или **Apache Kafka**

### 4.4 Инфраструктура
- **Docker:** контейнеризация
- **Docker Compose:** оркестрация контейнеров
- **Eureka:** service discovery
- **Spring Cloud Config:** централизованная конфигурация
- **Spring Cloud Gateway:** API Gateway
- **Zipkin:** distributed tracing
- **Prometheus:** мониторинг
- **Grafana:** визуализация метрик

### 4.5 Тестирование
- **JUnit 5:** unit тесты
- **Mockito:** моки
- **TestContainers:** интеграционные тесты
- **REST Assured:** тестирование API

---

## 5. Нефункциональные требования

### 5.1 Производительность
- Время отклика API Gateway: < 200ms (95 percentile)
- Поддержка 100 одновременных пользователей
- Throughput: минимум 500 requests/second на Gateway

### 5.2 Безопасность
- JWT токены для аутентификации
- HTTPS для всех внешних соединений
- Хеширование паролей (BCrypt)
- Rate limiting на API Gateway
- CORS конфигурация

### 5.3 Масштабируемость
- Все сервисы должны быть stateless
- Возможность горизонтального масштабирования
- Использование load balancing через Eureka

### 5.4 Отказоустойчивость
- Circuit Breaker pattern (Resilience4j)
- Retry механизмы
- Timeout конфигурации
- Graceful degradation

### 5.5 Мониторинг и логирование
- Централизованное логирование
- Structured logging (JSON)
- Health checks для всех сервисов
- Метрики Prometheus
- Distributed tracing с Zipkin

---

## 6. План разработки

### Фаза 1: Инфраструктура (1-2 недели)
1. Настройка multi-module Gradle проекта
2. Конфигурация Eureka Server
3. Конфигурация Config Server
4. Настройка Docker Compose для баз данных
5. Базовая настройка API Gateway

### Фаза 2: Core сервисы (2-3 недели)
1. User Service (регистрация, аутентификация)
2. Product Service (CRUD товаров)
3. Интеграция с Eureka
4. Настройка баз данных

### Фаза 3: Бизнес-логика (2-3 недели)
1. Order Service
2. Payment Service
3. Интеграция между сервисами (REST)
4. Обработка ошибок

### Фаза 4: Event-Driven Architecture (1-2 недели)
1. Настройка Apache Kafka
2. Настройка Kafka Topics
3. Notification Service
4. Асинхронная обработка событий
5. Интеграция через Kafka сообщения

### Фаза 5: Мониторинг и DevOps (1-2 недели)
1. Настройка Zipkin
2. Настройка Prometheus и Grafana
3. Docker образы для всех сервисов
4. Docker Compose для всей системы
5. CI/CD pipeline (опционально)

### Фаза 6: Тестирование и документация (1-2 недели)
1. Unit тесты
2. Интеграционные тесты
3. API документация (Swagger/OpenAPI)
4. README и инструкции по запуску

---

## 7. Структура проекта

```
SimpleMicroserviceProject/
├── api-gateway/
│   ├── src/
│   └── build.gradle.kts
├── eureka-server/
│   ├── src/
│   └── build.gradle.kts
├── config-server/
│   ├── src/
│   └── build.gradle.kts
├── user-service/
│   ├── src/
│   └── build.gradle.kts
├── product-service/
│   ├── src/
│   └── build.gradle.kts
├── order-service/
│   ├── src/
│   └── build.gradle.kts
├── payment-service/
│   ├── src/
│   └── build.gradle.kts
├── notification-service/
│   ├── src/
│   └── build.gradle.kts
├── common/
│   ├── src/
│   └── build.gradle.kts
├── config-repo/          (Git repo для конфигураций)
├── docker/
│   ├── docker-compose.yml
│   └── Dockerfile templates
├── docs/
│   └── api-documentation/
├── build.gradle.kts      (root)
└── settings.gradle.kts
```

---

## 8. Примеры сценариев использования

### 8.1 Регистрация и вход
1. Пользователь регистрируется через `POST /api/users/register`
2. User Service создает пользователя в БД
3. Пользователь входит через `POST /api/users/login`
4. User Service возвращает JWT токен
5. Токен используется для всех последующих запросов

### 8.2 Создание заказа
1. Пользователь получает список товаров `GET /api/products`
2. Пользователь добавляет товары в корзину (frontend state)
3. Пользователь создает заказ `POST /api/orders` с JWT токеном
4. Order Service:
   - Проверяет наличие товаров в Product Service
   - Создает заказ со статусом PENDING
   - Публикует событие `OrderCreatedEvent`
5. Payment Service обрабатывает платеж
   - Публикует событие `PaymentProcessedEvent`
6. Order Service обновляет статус на PAID
7. Notification Service отправляет email подтверждение

### 8.3 Обработка заказа (Admin)
1. Админ получает список заказов
2. Админ обновляет статус на SHIPPED
3. Order Service публикует событие
4. Notification Service отправляет уведомление клиенту

---

## 9. Дополнительные возможности (для продвинутого уровня)

### 9.1 Frontend
- React/Angular/Vue приложение
- Взаимодействие с API Gateway

### 9.2 Advanced Features
- Redis для кэширования
- Elasticsearch для поиска товаров
- WebSocket для real-time уведомлений
- API rate limiting и throttling
- API versioning
- Multi-tenancy support

### 9.3 DevOps
- Kubernetes deployment
- Helm charts
- GitHub Actions / Jenkins CI/CD
- Automated testing pipeline

---

## 10. Метрики успеха проекта

### 10.1 Технические метрики
- [ ] Все сервисы успешно регистрируются в Eureka
- [ ] API Gateway корректно маршрутизирует запросы
- [ ] Все unit тесты проходят (coverage > 80%)
- [ ] Интеграционные тесты проходят
- [ ] Система запускается одной командой через Docker Compose
- [ ] Трассировка работает в Zipkin
- [ ] Метрики отображаются в Grafana

### 10.2 Функциональные метрики
- [ ] Пользователь может зарегистрироваться и войти
- [ ] Пользователь может просматривать товары
- [ ] Пользователь может создать заказ
- [ ] Платеж обрабатывается корректно
- [ ] Уведомления отправляются при изменении статуса
- [ ] Админ может управлять товарами и заказами

### 10.3 Обучающие цели
После завершения проекта вы изучите:
- [x] Микросервисную архитектуру
- [x] Service Discovery (Eureka)
- [x] API Gateway pattern
- [x] Event-Driven Architecture
- [x] Distributed Tracing
- [x] Containerization (Docker)
- [x] Apache Kafka для асинхронного взаимодействия
- [x] REST API design
- [x] JWT аутентификацию
- [x] Database per service pattern
- [x] Circuit Breaker pattern
- [x] Monitoring и Observability

---

## 11. Ресурсы для обучения

### Документация
- Spring Cloud: https://spring.io/projects/spring-cloud
- Spring Boot: https://spring.io/projects/spring-boot
- RabbitMQ: https://www.rabbitmq.com/
- Docker: https://docs.docker.com/

### Книги
- "Building Microservices" - Sam Newman
- "Spring Microservices in Action" - John Carnell
- "Cloud Native Java" - Josh Long

### Видео курсы
- Spring Boot Microservices на YouTube
- Baeldung Spring tutorials

---

## 12. Контакты и поддержка

Для вопросов и обсуждений создавайте issues в репозитории проекта.

---

**Версия:** 1.0  
**Дата:** 2025-01-21  
**Статус:** Утверждено для разработки

