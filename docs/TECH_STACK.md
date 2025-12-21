# Технологический стек проекта

## Версии и зависимости для всех микросервисов

---

## 1. Базовые технологии

### 1.1 Java
- **Версия:** Java 17 (LTS)
- **Альтернатива:** Java 21 (новый LTS)
- **Причина:** Стабильная LTS версия с современными возможностями

### 1.2 Система сборки
- **Gradle:** 8.5+
- **Формат:** Kotlin DSL (`build.gradle.kts`)
- **Wrapper:** включен в проект

### 1.3 Spring Framework
- **Spring Boot:** 3.2.1
- **Spring Cloud:** 2023.0.0
- **Причина:** Последние стабильные версии с полной совместимостью

---

## 2. Spring Cloud компоненты

### 2.1 Service Discovery
```kotlin
// Eureka Server & Client
implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")
implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
```
- **Компонент:** Netflix Eureka
- **Назначение:** Регистрация и обнаружение сервисов

### 2.2 Configuration Management
```kotlin
// Config Server
implementation("org.springframework.cloud:spring-cloud-config-server")

// Config Client (для всех сервисов)
implementation("org.springframework.cloud:spring-cloud-starter-config")
```
- **Компонент:** Spring Cloud Config
- **Назначение:** Централизованное управление конфигурациями

### 2.3 API Gateway
```kotlin
implementation("org.springframework.cloud:spring-cloud-starter-gateway")
```
- **Компонент:** Spring Cloud Gateway
- **Назначение:** Маршрутизация, аутентификация, rate limiting

### 2.4 Circuit Breaker
```kotlin
implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
implementation("io.github.resilience4j:resilience4j-spring-boot3:2.1.0")
```
- **Компонент:** Resilience4j
- **Назначение:** Отказоустойчивость, retry, timeout

---

## 3. Базы данных

### 3.1 PostgreSQL
- **Версия:** 15-alpine (Docker образ)
- **Driver:** `org.postgresql:postgresql` (runtime)
- **ORM:** Spring Data JPA

**Порты для разных сервисов:**
- User Service: 5432
- Product Service: 5433
- Order Service: 5434
- Payment Service: 5435
- Notification Service: 5436

**Зависимости:**
```kotlin
implementation("org.springframework.boot:spring-boot-starter-data-jpa")
runtimeOnly("org.postgresql:postgresql")
```

### 3.2 Миграции БД (опционально)
```kotlin
// Flyway
implementation("org.flywaydb:flyway-core")

// или Liquibase
implementation("org.liquibase:liquibase-core")
```

---

## 4. Security и Authentication

### 4.1 Spring Security
```kotlin
implementation("org.springframework.boot:spring-boot-starter-security")
```

### 4.2 JWT
```kotlin
implementation("io.jsonwebtoken:jjwt-api:0.12.3")
runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
```
- **Библиотека:** JJWT (Java JWT)
- **Назначение:** Создание и валидация JWT токенов

### 4.3 Password Encoding
- **BCrypt:** встроен в Spring Security
- **Назначение:** Хеширование паролей

---

## 5. Message Broker

### 5.1 RabbitMQ (рекомендуется)
- **Версия:** 3-management-alpine (Docker)
- **Порты:** 
  - AMQP: 5672
  - Management UI: 15672

**Зависимости:**
```kotlin
implementation("org.springframework.boot:spring-boot-starter-amqp")
implementation("org.springframework.cloud:spring-cloud-starter-stream-rabbit")
```

### 5.2 Kafka (альтернатива)
- **Версия:** 3.6
- **Порт:** 9092

**Зависимости:**
```kotlin
implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")
```

---

## 6. Monitoring и Observability

### 6.1 Actuator
```kotlin
implementation("org.springframework.boot:spring-boot-starter-actuator")
```
- **Назначение:** Health checks, metrics, info endpoints

### 6.2 Distributed Tracing
```kotlin
// Micrometer Tracing с Brave (для Zipkin)
implementation("io.micrometer:micrometer-tracing-bridge-brave")
implementation("io.zipkin.reporter2:zipkin-reporter-brave")
```
- **Zipkin версия:** latest (Docker)
- **Порт:** 9411

### 6.3 Prometheus
```kotlin
implementation("io.micrometer:micrometer-registry-prometheus")
```
- **Prometheus версия:** latest (Docker)
- **Порт:** 9090

### 6.4 Grafana
- **Версия:** latest (Docker)
- **Порт:** 3000
- **Credentials:** admin/admin

---

## 7. Утилиты и Helper библиотеки

### 7.1 Lombok
```kotlin
compileOnly("org.projectlombok:lombok")
annotationProcessor("org.projectlombok:lombok")
```
- **Назначение:** Уменьшение boilerplate кода (@Data, @Builder, etc.)

### 7.2 MapStruct
```kotlin
implementation("org.mapstruct:mapstruct:1.5.5.Final")
annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
```
- **Назначение:** Маппинг между Entity и DTO

### 7.3 Validation
```kotlin
implementation("org.springframework.boot:spring-boot-starter-validation")
```
- **Назначение:** Валидация входных данных (@Valid, @NotNull, etc.)

---

## 8. Тестирование

### 8.1 Unit тесты
```kotlin
testImplementation("org.springframework.boot:spring-boot-starter-test")
testImplementation("org.mockito:mockito-core")
testImplementation("org.mockito:mockito-junit-jupiter")
```

### 8.2 Integration тесты
```kotlin
// TestContainers для интеграционных тестов с БД
testImplementation("org.springframework.boot:spring-boot-testcontainers")
testImplementation("org.testcontainers:postgresql:1.19.3")
testImplementation("org.testcontainers:junit-jupiter:1.19.3")
testImplementation("org.testcontainers:rabbitmq:1.19.3")
```

### 8.3 API тестирование
```kotlin
testImplementation("io.rest-assured:rest-assured:5.4.0")
```

### 8.4 Security тестирование
```kotlin
testImplementation("org.springframework.security:spring-security-test")
```

---

## 9. DevTools (для разработки)

```kotlin
developmentOnly("org.springframework.boot:spring-boot-devtools")
```
- **Назначение:** Hot reload, LiveReload

---

## 10. Docker

### 10.1 Docker образы
- **PostgreSQL:** `postgres:15-alpine`
- **RabbitMQ:** `rabbitmq:3-management-alpine`
- **Zipkin:** `openzipkin/zipkin`
- **Prometheus:** `prom/prometheus`
- **Grafana:** `grafana/grafana`

### 10.2 Docker Compose
- **Версия:** 3.8

---

## 11. Полный BOM (Bill of Materials)

### Root build.gradle.kts

```kotlin
plugins {
    id("java")
    id("org.springframework.boot") version "3.2.1" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

allprojects {
    group = "com.ecommerce"
    version = "1.0.0"
    
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    
    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    dependencies {
        // Общие зависимости для всех модулей
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }
    
    dependencyManagement {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.0")
        }
    }
    
    tasks.test {
        useJUnitPlatform()
    }
}
```

---

## 12. Специфичные зависимости по сервисам

### 12.1 Eureka Server
```kotlin
dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}
```

### 12.2 Config Server
```kotlin
dependencies {
    implementation("org.springframework.cloud:spring-cloud-config-server")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}
```

### 12.3 API Gateway
```kotlin
dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // JWT validation
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
    
    // Circuit Breaker
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    
    // Tracing
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")
}
```

### 12.4 User Service
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
    
    // Database
    runtimeOnly("org.postgresql:postgresql")
    
    // MapStruct
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    
    // Tracing
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")
    
    // Prometheus
    implementation("io.micrometer:micrometer-registry-prometheus")
}
```

### 12.5 Product Service
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    
    runtimeOnly("org.postgresql:postgresql")
    
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")
    implementation("io.micrometer:micrometer-registry-prometheus")
}
```

### 12.6 Order Service
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    
    // RabbitMQ для event-driven
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-rabbit")
    
    // WebClient для вызова других сервисов
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    
    // Circuit Breaker
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    
    runtimeOnly("org.postgresql:postgresql")
    
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")
    implementation("io.micrometer:micrometer-registry-prometheus")
}
```

### 12.7 Payment Service
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-rabbit")
    
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    
    runtimeOnly("org.postgresql:postgresql")
    
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")
    implementation("io.micrometer:micrometer-registry-prometheus")
}
```

### 12.8 Notification Service
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    
    // RabbitMQ для получения событий
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-rabbit")
    
    // Mail sender (для email уведомлений)
    implementation("org.springframework.boot:spring-boot-starter-mail")
    
    runtimeOnly("org.postgresql:postgresql")
    
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")
    implementation("io.micrometer:micrometer-registry-prometheus")
}
```

### 12.9 Common Module (shared code)
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // Общие DTO, события, утилиты
}
```

---

## 13. Переменные окружения

### Для локальной разработки (.env файл)
```bash
# Eureka
EUREKA_SERVER_URL=http://localhost:8761/eureka/

# Config Server
CONFIG_SERVER_URL=http://localhost:8888

# PostgreSQL
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_ZOOKEEPER_CONNECT=localhost:2181

# JWT
JWT_SECRET=your-secret-key-change-this-in-production-minimum-256-bits
JWT_EXPIRATION=86400000

# Zipkin
ZIPKIN_URL=http://localhost:9411
```

---

## 14. Минимальные системные требования

### Для разработки
- **RAM:** 8GB минимум, 16GB рекомендуется
- **CPU:** 4 ядра минимум
- **Disk:** 10GB свободного места
- **OS:** Windows 10/11, macOS 12+, Linux (Ubuntu 20.04+)

### Для Docker
- **Docker:** 20.10+
- **Docker Compose:** 2.0+
- **Выделенная память для Docker:** минимум 4GB

---

## 15. IDE и плагины

### IntelliJ IDEA
- **Версия:** 2023.2+
- **Edition:** Community или Ultimate

**Рекомендуемые плагины:**
- Lombok
- Spring Boot
- Database Navigator
- Docker
- .env files support

### VS Code (альтернатива)
**Расширения:**
- Extension Pack for Java
- Spring Boot Extension Pack
- Docker
- Gradle for Java

---

## 16. Порядок установки зависимостей

1. **JDK 17** - установить первым
2. **Gradle** - через wrapper (./gradlew)
3. **Docker** - для баз данных и инфраструктуры
4. **IDE** - IntelliJ IDEA или VS Code
5. **Git** - для version control

---

**Последнее обновление:** 2025-01-21  
**Статус:** Утверждено для разработки

