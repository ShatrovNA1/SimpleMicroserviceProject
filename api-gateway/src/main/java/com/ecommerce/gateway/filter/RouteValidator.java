package com.ecommerce.gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

/**
 * Валидатор маршрутов для определения открытых endpoints
 * Открытые endpoints не требуют JWT аутентификации
 */
@Component
public class RouteValidator {

    /**
     * Список открытых endpoints, не требующих аутентификации
     */
    public static final List<String> OPEN_ENDPOINTS = List.of(
            // User Service - регистрация и вход
            "/api/users/register",
            "/api/users/login",
            "/api/users/refresh-token",

            // Product Service - просмотр товаров (публичный доступ)
            "/api/products",
            "/api/products/search",
            "/api/categories",

            // Actuator endpoints
            "/actuator",
            "/actuator/health",
            "/actuator/info",

            // Fallback endpoints
            "/fallback"
    );

    /**
     * Предикат для проверки, является ли endpoint открытым
     */
    public Predicate<ServerHttpRequest> isSecured = request ->
            OPEN_ENDPOINTS.stream()
                    .noneMatch(uri -> request.getURI().getPath().startsWith(uri));

    /**
     * Проверяет, является ли endpoint открытым (не требует аутентификации)
     * @param request HTTP запрос
     * @return true если endpoint открытый
     */
    public boolean isOpenEndpoint(ServerHttpRequest request) {
        String path = request.getURI().getPath();

        // Проверяем точное совпадение или префикс
        return OPEN_ENDPOINTS.stream().anyMatch(openPath -> {
            // Для GET запросов на /api/products и /api/categories разрешаем доступ
            if (path.equals(openPath) || path.startsWith(openPath + "/")) {
                return true;
            }
            // Специальная обработка для GET запросов к товарам (с query параметрами)
            if (openPath.equals("/api/products") && path.startsWith("/api/products")) {
                String method = request.getMethod() != null ? request.getMethod().name() : "";
                return method.equals("GET");
            }
            if (openPath.equals("/api/categories") && path.startsWith("/api/categories")) {
                String method = request.getMethod() != null ? request.getMethod().name() : "";
                return method.equals("GET");
            }
            return false;
        });
    }
}

