package com.ecommerce.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Server Application - централизованное управление конфигурациями
 * Порт: 8888
 *
 * Функции:
 * - Хранение конфигураций в локальном репозитории
 * - Предоставление конфигураций другим сервисам
 * - Регистрация в Eureka Server
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}

