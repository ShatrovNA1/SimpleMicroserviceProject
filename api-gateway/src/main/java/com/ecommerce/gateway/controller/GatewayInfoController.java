package com.ecommerce.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Контроллер для проверки здоровья API Gateway
 * Предоставляет дополнительную информацию о статусе сервиса
 */
@RestController
@RequestMapping("/api/gateway")
public class GatewayInfoController {

    @GetMapping("/status")
    public Mono<ResponseEntity<Map<String, Object>>> getStatus() {
        Map<String, Object> status = Map.of(
                "status", "UP",
                "service", "API Gateway",
                "timestamp", LocalDateTime.now().toString(),
                "version", "1.0-SNAPSHOT"
        );

        return Mono.just(ResponseEntity.ok(status));
    }

    @GetMapping("/info")
    public Mono<ResponseEntity<Map<String, Object>>> getInfo() {
        Map<String, Object> info = Map.of(
                "service", "API Gateway",
                "description", "Единая точка входа для E-Commerce Microservices Platform",
                "version", "1.0-SNAPSHOT",
                "port", 8080,
                "features", Map.of(
                        "authentication", "JWT",
                        "rateLimiting", "100 requests/minute",
                        "circuitBreaker", "Resilience4j",
                        "serviceDiscovery", "Eureka"
                ),
                "routes", Map.of(
                        "user-service", "/api/users/**",
                        "product-service", "/api/products/**, /api/categories/**",
                        "order-service", "/api/orders/**",
                        "payment-service", "/api/payments/**",
                        "notification-service", "/api/notifications/**"
                )
        );

        return Mono.just(ResponseEntity.ok(info));
    }
}

