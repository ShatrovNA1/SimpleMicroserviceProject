package com.ecommerce.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Контроллер для fallback ответов при недоступности микросервисов
 * Используется Circuit Breaker для перенаправления на эти endpoints
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @GetMapping("/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        log.warn("Fallback triggered for User Service");
        return Mono.just(buildFallbackResponse("User Service"));
    }

    @GetMapping("/product-service")
    public Mono<ResponseEntity<Map<String, Object>>> productServiceFallback() {
        log.warn("Fallback triggered for Product Service");
        return Mono.just(buildFallbackResponse("Product Service"));
    }

    @GetMapping("/order-service")
    public Mono<ResponseEntity<Map<String, Object>>> orderServiceFallback() {
        log.warn("Fallback triggered for Order Service");
        return Mono.just(buildFallbackResponse("Order Service"));
    }

    @GetMapping("/payment-service")
    public Mono<ResponseEntity<Map<String, Object>>> paymentServiceFallback() {
        log.warn("Fallback triggered for Payment Service");
        return Mono.just(buildFallbackResponse("Payment Service"));
    }

    @GetMapping("/notification-service")
    public Mono<ResponseEntity<Map<String, Object>>> notificationServiceFallback() {
        log.warn("Fallback triggered for Notification Service");
        return Mono.just(buildFallbackResponse("Notification Service"));
    }

    @GetMapping("/{serviceName}")
    public Mono<ResponseEntity<Map<String, Object>>> genericFallback(@PathVariable String serviceName) {
        log.warn("Fallback triggered for service: {}", serviceName);
        return Mono.just(buildFallbackResponse(serviceName));
    }

    /**
     * Построение стандартного fallback ответа
     */
    private ResponseEntity<Map<String, Object>> buildFallbackResponse(String serviceName) {
        Map<String, Object> response = Map.of(
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error", "Service Unavailable",
                "message", String.format("%s is currently unavailable. Please try again later.", serviceName),
                "timestamp", LocalDateTime.now().toString(),
                "service", serviceName
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}

