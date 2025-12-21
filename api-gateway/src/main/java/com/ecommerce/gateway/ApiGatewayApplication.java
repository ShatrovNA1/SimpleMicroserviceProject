package com.ecommerce.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway Application - единая точка входа для всех микросервисов
 * Порт: 8080
 *
 * Основные функции:
 * - Маршрутизация запросов к микросервисам
 * - JWT аутентификация и авторизация
 * - Rate limiting
 * - Circuit Breaker
 * - Логирование запросов
 * - CORS конфигурация
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}

