package com.ecommerce.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server Application - Service Registry для микросервисной архитектуры
 * Порт: 8761
 * Основные функции:
 * - Регистрация всех микросервисов
 * - Service Discovery
 * - Health checking
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}

