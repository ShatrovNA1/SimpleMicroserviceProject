# Eureka Server - Service Registry

## Описание
Eureka Server является центральным компонентом Service Discovery в микросервисной архитектуре. Все остальные сервисы регистрируются в Eureka и могут находить друг друга через него.

## Порт
- **8761**

## Технологии
- Spring Boot 3.2.1
- Spring Cloud Netflix Eureka Server 2023.0.0
- Java 17

## Основные функции
✅ Регистрация всех микросервисов  
✅ Service Discovery (поиск сервисов)  
✅ Health checking (проверка работоспособности)  
✅ Load balancing (балансировка нагрузки)  

## Запуск

### 1. Сборка проекта
```bash
cd /home/cactusjack/IdeaProjects/SimpleMicroserviceProject
./gradlew :eureka-server:build
```

### 2. Запуск сервера
```bash
./gradlew :eureka-server:bootRun
```

### 3. Проверка работы
После запуска откройте в браузере:
```
http://localhost:8761
```

Вы увидите Eureka Dashboard с информацией о зарегистрированных сервисах.

## Endpoints

### Eureka Dashboard
- **URL:** http://localhost:8761
- **Описание:** Web UI для просмотра зарегистрированных сервисов

### Health Check
- **URL:** http://localhost:8761/actuator/health
- **Описание:** Проверка работоспособности сервера

### Metrics
- **URL:** http://localhost:8761/actuator/metrics
- **Описание:** Метрики сервера

## Конфигурация

### application.yml
Основные настройки:
- `server.port: 8761` - порт сервера
- `eureka.client.register-with-eureka: false` - сам Eureka не регистрируется в себе
- `eureka.client.fetch-registry: false` - не загружает реестр сервисов
- `eureka.server.enable-self-preservation: false` - отключен self-preservation mode (для dev)

## Как другие сервисы регистрируются в Eureka

В других сервисах нужно:

1. Добавить зависимость:
```kotlin
implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
```

2. Добавить конфигурацию в `application.yml`:
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

3. Аннотация `@EnableDiscoveryClient` или `@EnableEurekaClient` (опционально для новых версий Spring Cloud)

## Проверка работы

### Через Dashboard
1. Откройте http://localhost:8761
2. Проверьте секцию "Instances currently registered with Eureka"
3. После запуска других сервисов они появятся в списке

### Через API
```bash
curl http://localhost:8761/eureka/apps
```

## Troubleshooting

### Eureka не запускается
- Проверьте, что порт 8761 свободен: `netstat -tuln | grep 8761`
- Проверьте логи в консоли

### Сервисы не регистрируются
- Убедитесь, что Eureka Server запущен
- Проверьте конфигурацию `eureka.client.service-url.defaultZone` в клиентах
- Проверьте логи клиентских сервисов

## Следующие шаги
После успешного запуска Eureka Server можно переходить к созданию:
1. **Config Server** - централизованная конфигурация
2. **API Gateway** - единая точка входа
3. **User Service** - первый бизнес-сервис

## Полезные ссылки
- [Spring Cloud Netflix Documentation](https://spring.io/projects/spring-cloud-netflix)
- [Eureka Wiki](https://github.com/Netflix/eureka/wiki)

