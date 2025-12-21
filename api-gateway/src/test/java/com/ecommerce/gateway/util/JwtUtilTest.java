package com.ecommerce.gateway.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String TEST_SECRET = "mySecretKeyForJWTTokenGenerationAndValidation2025";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        jwtUtil.init();
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        String invalidToken = "invalid.jwt.token";
        assertFalse(jwtUtil.validateToken(invalidToken));
    }

    @Test
    void validateToken_MalformedToken_ReturnsFalse() {
        String malformedToken = "not-a-jwt-token";
        assertFalse(jwtUtil.validateToken(malformedToken));
    }

    @Test
    void validateToken_EmptyToken_ReturnsFalse() {
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    void validateToken_NullToken_ReturnsFalse() {
        assertFalse(jwtUtil.validateToken(null));
    }
}

