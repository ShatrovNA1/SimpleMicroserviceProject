# Apache Kafka - Краткий справочник для проекта

## Что такое Apache Kafka

Apache Kafka - это распределенная платформа потоковой передачи данных (distributed streaming platform), используемая для:
- Асинхронного обмена сообщениями между микросервисами
- Event-driven архитектуры
- Обработки потоков данных в реальном времени

---

## Основные концепции

### 1. Topics (Топики)
Категории или каналы, в которые публикуются сообщения.

**Топики в нашем проекте:**
```
order-events        - события заказов
payment-events      - события платежей
stock-events        - обновления запасов
notification-events - события для уведомлений
```

### 2. Producers (Производители)
Сервисы, которые публикуют сообщения в топики.

**Примеры:**
- Order Service → публикует в `order-events`
- Payment Service → публикует в `payment-events`

### 3. Consumers (Потребители)
Сервисы, которые читают сообщения из топиков.

**Примеры:**
- Notification Service → читает из `order-events`, `payment-events`
- Order Service → читает из `payment-events`

### 4. Consumer Groups
Группы потребителей для параллельной обработки и балансировки нагрузки.

---

## Конфигурация Kafka в проекте

### Docker Compose (docker/docker-compose.yml)

```yaml
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"
    volumes:
      - zookeeper-data:/var/lib/zookeeper/data

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
      - "9093:9093"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092,PLAINTEXT_INTERNAL://kafka:9093
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT_INTERNAL
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
    volumes:
      - kafka-data:/var/lib/kafka/data

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: kafka-ui
    depends_on:
      - kafka
    ports:
      - "8090:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9093
      KAFKA_CLUSTERS_0_ZOOKEEPER: zookeeper:2181

volumes:
  zookeeper-data:
  kafka-data:
```

### Spring Boot Конфигурация

#### application.yml (общая для всех сервисов)

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: ${spring.application.name}
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

---

## Примеры использования в коде

### 1. Producer (Order Service)

#### Event класс
```java
package com.ecommerce.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
}
```

#### Kafka Producer Config
```java
package com.ecommerce.order.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }
    
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

#### Использование в Service
```java
package com.ecommerce.order.service;

import com.ecommerce.order.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {
    
    private static final String ORDER_EVENTS_TOPIC = "order-events";
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent: {}", event);
        kafkaTemplate.send(ORDER_EVENTS_TOPIC, event.getOrderId().toString(), event);
    }
}
```

### 2. Consumer (Notification Service)

#### Kafka Consumer Config
```java
package com.ecommerce.notification.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.ecommerce.notification.event.OrderCreatedEvent");
        return new DefaultKafkaConsumerFactory<>(config);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
```

#### Event Listener
```java
package com.ecommerce.notification.listener;

import com.ecommerce.notification.event.OrderCreatedEvent;
import com.ecommerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {
    
    private final NotificationService notificationService;
    
    @KafkaListener(topics = "order-events", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: {}", event);
        
        // Отправить email уведомление
        notificationService.sendOrderConfirmationEmail(
            event.getUserId(),
            event.getOrderNumber(),
            event.getTotalAmount()
        );
    }
}
```

---

## Структура топиков и событий

### Topic: order-events

**События:**
1. **OrderCreatedEvent**
   ```json
   {
     "orderId": 1,
     "orderNumber": "ORD-2025-001",
     "userId": 1,
     "totalAmount": 1299.99,
     "status": "PENDING",
     "createdAt": "2025-01-21T10:00:00"
   }
   ```

2. **OrderStatusChangedEvent**
   ```json
   {
     "orderId": 1,
     "orderNumber": "ORD-2025-001",
     "oldStatus": "PENDING",
     "newStatus": "PAID",
     "updatedAt": "2025-01-21T10:05:00"
   }
   ```

### Topic: payment-events

**События:**
1. **PaymentProcessedEvent**
   ```json
   {
     "paymentId": 1,
     "orderId": 1,
     "amount": 1299.99,
     "status": "SUCCESS",
     "transactionId": "TXN-2025-001",
     "processedAt": "2025-01-21T10:05:00"
   }
   ```

### Topic: stock-events

**События:**
1. **StockUpdatedEvent**
   ```json
   {
     "productId": 1,
     "oldQuantity": 50,
     "newQuantity": 48,
     "reason": "ORDER_PLACED",
     "updatedAt": "2025-01-21T10:00:00"
   }
   ```

---

## Полезные команды

### Управление Kafka через Docker

```bash
# Список топиков
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list

# Создать топик
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 \
  --create --topic order-events --partitions 3 --replication-factor 1

# Описание топика
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 \
  --describe --topic order-events

# Удалить топик
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 \
  --delete --topic order-events

# Чтение сообщений из топика
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic order-events --from-beginning

# Отправка тестового сообщения
docker exec -it kafka kafka-console-producer --bootstrap-server localhost:9092 \
  --topic order-events
```

### Проверка Consumer Groups

```bash
# Список consumer groups
docker exec -it kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list

# Описание consumer group
docker exec -it kafka kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group notification-service
```

---

## Kafka UI

После запуска Docker Compose доступен веб-интерфейс:
- **URL:** http://localhost:8090
- **Функции:**
  - Просмотр топиков и сообщений
  - Мониторинг consumer groups
  - Отправка тестовых сообщений
  - Просмотр конфигурации

---

## Best Practices

### 1. Naming Convention для топиков
```
{domain}-{entity}-{event-type}
Примеры:
- order-events
- payment-events
- stock-events
```

### 2. Event Schema
Всегда включайте:
- Уникальный ID события
- Timestamp
- Версию схемы (для совместимости)

### 3. Error Handling
```java
@KafkaListener(topics = "order-events")
public void handleEvent(OrderCreatedEvent event) {
    try {
        // Обработка события
        processOrder(event);
    } catch (Exception e) {
        log.error("Error processing event: {}", event, e);
        // Отправить в Dead Letter Topic
        kafkaTemplate.send("order-events-dlq", event);
    }
}
```

### 4. Idempotency
Обеспечьте идемпотентность обработчиков событий:
```java
@Transactional
public void handleOrderCreated(OrderCreatedEvent event) {
    // Проверка, не обработано ли уже
    if (processedEvents.exists(event.getEventId())) {
        log.info("Event already processed: {}", event.getEventId());
        return;
    }
    
    // Обработка
    processOrder(event);
    
    // Сохранить ID обработанного события
    processedEvents.save(event.getEventId());
}
```

### 5. Dead Letter Queue (DLQ)
Для обработки ошибок создайте DLQ топики:
```
order-events-dlq
payment-events-dlq
```

---

## Мониторинг

### Метрики для отслеживания

1. **Producer Metrics:**
   - Throughput (сообщений/сек)
   - Latency
   - Error rate

2. **Consumer Metrics:**
   - Lag (отставание обработки)
   - Throughput
   - Processing time

3. **Broker Metrics:**
   - CPU/Memory usage
   - Disk I/O
   - Network throughput

### Prometheus + Grafana

Добавьте в application.yml:
```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

---

## Troubleshooting

### Проблема: Consumer не получает сообщения

**Решение:**
1. Проверьте, что consumer group ID правильный
2. Проверьте offset (может быть уже в конце топика)
3. Проверьте десериализацию

```bash
# Сбросить offset на начало
docker exec -it kafka kafka-consumer-groups --bootstrap-server localhost:9092 \
  --group notification-service --reset-offsets --to-earliest --execute --topic order-events
```

### Проблема: Kafka не запускается

**Решение:**
1. Проверьте, что Zookeeper работает
2. Проверьте порты (9092, 2181)
3. Очистите volumes:
```bash
docker-compose down -v
docker-compose up -d
```

### Проблема: Большой lag у consumer

**Решение:**
1. Увеличьте количество partitions
2. Добавьте больше consumer instances
3. Оптимизируйте обработку сообщений

---

## Дополнительные ресурсы

- **Документация:** https://kafka.apache.org/documentation/
- **Spring Kafka:** https://spring.io/projects/spring-kafka
- **Confluent Documentation:** https://docs.confluent.io/

---

**Последнее обновление:** 2025-01-21

