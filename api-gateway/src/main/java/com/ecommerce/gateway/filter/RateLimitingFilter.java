package com.ecommerce.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Глобальный фильтр для Rate Limiting
 * Ограничивает количество запросов с одного IP адреса
 */
@Component
@Slf4j
public class RateLimitingFilter implements GlobalFilter, Ordered {

    // Хранилище для подсчета запросов по IP
    private final ConcurrentHashMap<String, RateLimitInfo> requestCounts = new ConcurrentHashMap<>();

    // Максимальное количество запросов в минуту с одного IP
    private static final int MAX_REQUESTS_PER_MINUTE = 100;

    // Окно времени для подсчета запросов (в миллисекундах)
    private static final long TIME_WINDOW_MS = 60_000;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String clientIp = getClientIp(request);

        // Проверяем rate limit
        if (!isAllowed(clientIp)) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            return onRateLimitExceeded(exchange);
        }

        return chain.filter(exchange);
    }

    /**
     * Получает IP адрес клиента
     */
    private String getClientIp(ServerHttpRequest request) {
        // Проверяем заголовки прокси
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    /**
     * Проверяет, разрешен ли запрос для данного IP
     */
    private boolean isAllowed(String clientIp) {
        long currentTime = Instant.now().toEpochMilli();

        RateLimitInfo info = requestCounts.compute(clientIp, (ip, existing) -> {
            if (existing == null || currentTime - existing.windowStart > TIME_WINDOW_MS) {
                // Новое окно
                return new RateLimitInfo(currentTime, new AtomicInteger(1));
            } else {
                // Увеличиваем счетчик в текущем окне
                existing.requestCount.incrementAndGet();
                return existing;
            }
        });

        return info.requestCount.get() <= MAX_REQUESTS_PER_MINUTE;
    }

    /**
     * Обработка превышения rate limit
     */
    private Mono<Void> onRateLimitExceeded(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("Retry-After", "60");

        String body = "{\"error\": \"Rate limit exceeded. Please try again later.\", \"status\": 429}";

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes()))
        );
    }

    @Override
    public int getOrder() {
        // Выполняется одним из первых
        return -2;
    }

    /**
     * Информация о rate limiting для IP адреса
     */
    private static class RateLimitInfo {
        final long windowStart;
        final AtomicInteger requestCount;

        RateLimitInfo(long windowStart, AtomicInteger requestCount) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }
    }
}

