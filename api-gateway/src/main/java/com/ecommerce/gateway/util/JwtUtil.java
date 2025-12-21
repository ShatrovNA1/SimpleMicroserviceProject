package com.ecommerce.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Утилита для работы с JWT токенами
 * Валидация и извлечение данных из токенов
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationAndValidation2025}")
    private String secret;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // Создаем ключ из секрета
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Валидация JWT токена
     * @param token JWT токен
     * @return true если токен валидный
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !isTokenExpired(claims);
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Извлечение ID пользователя из токена
     */
    public String extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userId = claims.get("userId");
        return userId != null ? userId.toString() : claims.getSubject();
    }

    /**
     * Извлечение username из токена
     */
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        Object username = claims.get("username");
        return username != null ? username.toString() : claims.getSubject();
    }

    /**
     * Извлечение ролей пользователя из токена
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object roles = claims.get("roles");

        if (roles instanceof List) {
            return (List<String>) roles;
        }

        return List.of("ROLE_USER");
    }

    /**
     * Извлечение email из токена
     */
    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        Object email = claims.get("email");
        return email != null ? email.toString() : null;
    }

    /**
     * Извлечение всех claims из токена
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Проверка, истек ли токен
     */
    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.before(new Date());
    }
}

