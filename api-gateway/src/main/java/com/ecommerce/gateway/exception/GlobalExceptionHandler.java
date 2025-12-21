package com.ecommerce.gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Глобальный обработчик исключений для API Gateway
 * Обрабатывает все необработанные исключения и возвращает унифицированный ответ
 */
@Component
@Order(-2)
@Slf4j
public class GlobalExceptionHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = determineStatus(ex);
        String message = determineMessage(ex, status);

        log.error("Gateway error: {} - {} | Path: {}",
                status.value(),
                message,
                exchange.getRequest().getPath(),
                ex);

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"timestamp\": \"%s\", \"status\": %d, \"error\": \"%s\", \"message\": \"%s\", \"path\": \"%s\"}",
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                escapeJson(message),
                exchange.getRequest().getPath()
        );

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
        );
    }

    /**
     * Определение HTTP статуса на основе исключения
     */
    private HttpStatus determineStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException rse) {
            return HttpStatus.valueOf(rse.getStatusCode().value());
        }

        String exceptionName = ex.getClass().getSimpleName();

        return switch (exceptionName) {
            case "NotFoundException" -> HttpStatus.NOT_FOUND;
            case "UnauthorizedException" -> HttpStatus.UNAUTHORIZED;
            case "ForbiddenException" -> HttpStatus.FORBIDDEN;
            case "BadRequestException" -> HttpStatus.BAD_REQUEST;
            case "ServiceUnavailableException", "ConnectException" -> HttpStatus.SERVICE_UNAVAILABLE;
            case "TimeoutException" -> HttpStatus.GATEWAY_TIMEOUT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    /**
     * Определение сообщения об ошибке
     */
    private String determineMessage(Throwable ex, HttpStatus status) {
        if (ex instanceof ResponseStatusException rse) {
            return rse.getReason() != null ? rse.getReason() : status.getReasonPhrase();
        }

        if (status == HttpStatus.SERVICE_UNAVAILABLE) {
            return "The requested service is currently unavailable. Please try again later.";
        }

        if (status == HttpStatus.GATEWAY_TIMEOUT) {
            return "The service did not respond in time. Please try again later.";
        }

        String message = ex.getMessage();
        if (message != null && !message.isEmpty()) {
            return message;
        }

        return status.getReasonPhrase();
    }

    /**
     * Экранирование специальных символов для JSON
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

