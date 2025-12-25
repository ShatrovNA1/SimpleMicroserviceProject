# Product Service

Сервис управления товарами для E-Commerce платформы.

## Функции

- CRUD операции для товаров
- Управление категориями (иерархическая структура)
- Поиск товаров по ключевым словам
- Управление инвентарём (резервирование, возврат)
- Пагинация результатов

## Технологии

- Spring Boot 4.0
- Spring Data JPA
- PostgreSQL
- Eureka Client

## API Endpoints

### Products

| Метод  | Endpoint                      | Описание                          |
|--------|-------------------------------|-----------------------------------|
| POST   | /api/products                 | Создать товар                     |
| GET    | /api/products/{id}            | Получить товар по ID              |
| GET    | /api/products/sku/{sku}       | Получить товар по SKU             |
| GET    | /api/products                 | Список товаров (пагинация)        |
| GET    | /api/products/category/{id}   | Товары по категории               |
| GET    | /api/products/search?keyword= | Поиск товаров                     |
| POST   | /api/products/batch           | Получить товары по списку ID      |
| PUT    | /api/products/{id}            | Обновить товар                    |
| DELETE | /api/products/{id}            | Удалить товар                     |
| POST   | /api/products/{id}/reserve    | Зарезервировать товар             |
| POST   | /api/products/{id}/release    | Освободить резерв                 |
| GET    | /api/products/{id}/stock      | Проверить наличие                 |

### Categories

| Метод  | Endpoint                      | Описание                          |
|--------|-------------------------------|-----------------------------------|
| POST   | /api/categories               | Создать категорию                 |
| GET    | /api/categories/{id}          | Получить категорию                |
| GET    | /api/categories               | Список всех категорий             |
| GET    | /api/categories/root          | Корневые категории (с детьми)     |
| GET    | /api/categories/{id}/children | Дочерние категории                |
| PUT    | /api/categories/{id}          | Обновить категорию                |
| DELETE | /api/categories/{id}          | Удалить категорию                 |

## Примеры запросов

### Создать товар
```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 15",
    "description": "Latest Apple smartphone",
    "price": 999.99,
    "quantity": 100,
    "sku": "IPHONE-15",
    "categoryId": 1
  }'
```

### Поиск товаров
```bash
curl "http://localhost:8082/api/products/search?keyword=iphone&page=0&size=10"
```

### Резервирование товара
```bash
curl -X POST "http://localhost:8082/api/products/1/reserve?quantity=5"
```

## Тестовые данные

При запуске автоматически создаются:

**Категории:**
- Electronics → Smartphones, Laptops
- Clothing → Men's Clothing

**Товары:**
- iPhone 15 Pro ($999.99)
- Samsung Galaxy S24 ($899.99)
- MacBook Pro 14 ($1999.99)
- Dell XPS 15 ($1599.99)
- Classic T-Shirt ($29.99)

## Конфигурация

- Порт: 8082
- База данных: PostgreSQL (порт 5433)
- Eureka Server: http://localhost:8761/eureka/

## Запуск

### Локально
```bash
./gradlew :product-service:bootRun
```

### Docker
```bash
docker build -t product-service -f product-service/Dockerfile .
docker run -p 8082:8082 product-service
```

