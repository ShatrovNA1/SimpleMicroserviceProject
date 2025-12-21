package com.ecommerce.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import static org.junit.jupiter.api.Assertions.*;

class RouteValidatorTest {

    private final RouteValidator routeValidator = new RouteValidator();

    @Test
    void isOpenEndpoint_LoginEndpoint_ReturnsTrue() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/users/login")
                .build();

        assertTrue(routeValidator.isOpenEndpoint(request));
    }

    @Test
    void isOpenEndpoint_RegisterEndpoint_ReturnsTrue() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/users/register")
                .build();

        assertTrue(routeValidator.isOpenEndpoint(request));
    }

    @Test
    void isOpenEndpoint_ActuatorHealth_ReturnsTrue() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/actuator/health")
                .build();

        assertTrue(routeValidator.isOpenEndpoint(request));
    }

    @Test
    void isOpenEndpoint_GetProducts_ReturnsTrue() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/products")
                .build();

        assertTrue(routeValidator.isOpenEndpoint(request));
    }

    @Test
    void isOpenEndpoint_GetCategories_ReturnsTrue() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/categories")
                .build();

        assertTrue(routeValidator.isOpenEndpoint(request));
    }

    @Test
    void isOpenEndpoint_OrdersEndpoint_ReturnsFalse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/orders")
                .build();

        assertFalse(routeValidator.isOpenEndpoint(request));
    }

    @Test
    void isOpenEndpoint_PaymentsEndpoint_ReturnsFalse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/payments")
                .build();

        assertFalse(routeValidator.isOpenEndpoint(request));
    }

    @Test
    void isOpenEndpoint_UserProfileEndpoint_ReturnsFalse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/123")
                .build();

        assertFalse(routeValidator.isOpenEndpoint(request));
    }
}

