package com.ecommerce.gateway.filter;

import com.ecommerce.gateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Фильтр аутентификации для API Gateway
 * Проверяет JWT токен в заголовке Authorization
 */
@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;
    private final RouteValidator routeValidator;

    public AuthenticationFilter(JwtUtil jwtUtil, RouteValidator routeValidator) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.routeValidator = routeValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Пропускаем открытые endpoints
            if (routeValidator.isOpenEndpoint(request)) {
                return chain.filter(exchange);
            }

            // Проверяем наличие заголовка Authorization
            if (request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION) == null) {
                log.warn("Missing Authorization header for path: {}", request.getPath());
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Invalid Authorization header format for path: {}", request.getPath());
                return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // Валидация токена
                if (!jwtUtil.validateToken(token)) {
                    log.warn("Invalid JWT token for path: {}", request.getPath());
                    return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
                }

                // Извлекаем данные из токена и добавляем в заголовки
                String userId = jwtUtil.extractUserId(token);
                String username = jwtUtil.extractUsername(token);
                List<String> roles = jwtUtil.extractRoles(token);

                // Добавляем информацию о пользователе в заголовки для downstream сервисов
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-Username", username)
                        .header("X-User-Roles", String.join(",", roles))
                        .build();

                log.debug("Authenticated user: {} with roles: {}", username, roles);

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                log.error("Error validating JWT token: {}", e.getMessage());
                return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\": \"%s\", \"status\": %d}", message, status.value());

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(body.getBytes()))
        );
    }

    public static class Config {
        // Конфигурационные параметры фильтра (при необходимости)
    }
}

