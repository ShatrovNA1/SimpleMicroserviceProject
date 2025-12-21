package com.ecommerce.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Фильтр логирования для API Gateway
 * Логирует входящие запросы и исходящие ответы
 */
@Component
@Slf4j
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REQUEST_TIME_ATTR = "requestStartTime";

    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Генерируем уникальный ID запроса
            String requestId = UUID.randomUUID().toString();
            long startTime = Instant.now().toEpochMilli();

            // Логируем входящий запрос
            log.info(">>> Incoming Request: [{}] {} {} | Client: {} | Headers: {}",
                    requestId,
                    request.getMethod(),
                    request.getURI().getPath(),
                    request.getRemoteAddress(),
                    request.getHeaders().getFirst("Authorization") != null ? "[Authorization: ***]" : "[]"
            );

            // Добавляем Request ID в заголовки для трассировки
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header(REQUEST_ID_HEADER, requestId)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build())
                    .then(Mono.fromRunnable(() -> {
                        ServerHttpResponse response = exchange.getResponse();
                        long duration = Instant.now().toEpochMilli() - startTime;

                        // Логируем ответ
                        log.info("<<< Outgoing Response: [{}] {} {} | Status: {} | Duration: {}ms",
                                requestId,
                                request.getMethod(),
                                request.getURI().getPath(),
                                response.getStatusCode(),
                                duration
                        );

                        // Предупреждение о медленных запросах
                        if (duration > 1000) {
                            log.warn("!!! Slow request detected: [{}] {} {} took {}ms",
                                    requestId,
                                    request.getMethod(),
                                    request.getURI().getPath(),
                                    duration
                            );
                        }
                    }));
        };
    }

    public static class Config {
        // Конфигурационные параметры фильтра (при необходимости)
    }
}

