package com.ecommerce.gateway.config;

import com.ecommerce.gateway.filter.AuthenticationFilter;
import com.ecommerce.gateway.filter.LoggingFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация маршрутов API Gateway
 * Программная конфигурация дополняет YAML конфигурацию из Config Server
 */
@Configuration
public class GatewayConfig {

    private final AuthenticationFilter authenticationFilter;
    private final LoggingFilter loggingFilter;

    public GatewayConfig(AuthenticationFilter authenticationFilter, LoggingFilter loggingFilter) {
        this.authenticationFilter = authenticationFilter;
        this.loggingFilter = loggingFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service routes
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("user-service-cb")
                                        .setFallbackUri("forward:/fallback/user-service"))
                        )
                        .uri("lb://user-service"))

                // Product Service routes
                .route("product-service", r -> r
                        .path("/api/products/**", "/api/categories/**")
                        .filters(f -> f
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("product-service-cb")
                                        .setFallbackUri("forward:/fallback/product-service"))
                        )
                        .uri("lb://product-service"))

                // Order Service routes (требует аутентификации)
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("order-service-cb")
                                        .setFallbackUri("forward:/fallback/order-service"))
                        )
                        .uri("lb://order-service"))

                // Payment Service routes (требует аутентификации)
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("payment-service-cb")
                                        .setFallbackUri("forward:/fallback/payment-service"))
                        )
                        .uri("lb://payment-service"))

                // Notification Service routes (требует аутентификации)
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("notification-service-cb")
                                        .setFallbackUri("forward:/fallback/notification-service"))
                        )
                        .uri("lb://notification-service"))

                .build();
    }
}

