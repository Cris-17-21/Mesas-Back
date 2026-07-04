package com.restaurante.resturante.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.service.security.JwtService;

public class JwtServiceTest {

    private JwtService jwtService;
    private final String secretKey = "dGhpcy1pcy1hLXNlY3JldC1rZXktZm9yLWp3dC10ZXN0aW5nLXB1cnBvc2VzLW9ubHk=";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 86400000L); // 24 hours
        jwtService.init();
    }

    @Test
    @DisplayName("Debería retornar false cuando el token activo del usuario no coincide con el token validado")
    void isTokenValid_DifferentActiveToken_ReturnsFalse() {
        // GIVEN
        User user = new User();
        user.setUsername("testuser");
        user.setActive(true);

        String ip = "127.0.0.1";
        String token = jwtService.generateAccessToken(user, ip, null, null, null);

        // Set a different active token on user
        user.setTokenActivo("differentToken");

        // WHEN
        boolean isValid = jwtService.isTokenValid(token, user, ip);

        // THEN
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Debería retornar true cuando el token activo coincide o es nulo")
    void isTokenValid_MatchingActiveToken_ReturnsTrue() {
        // GIVEN
        User user = new User();
        user.setUsername("testuser");
        user.setActive(true);

        String ip = "127.0.0.1";
        String token = jwtService.generateAccessToken(user, ip, null, null, null);

        // Scenario 1: Active token matches
        user.setTokenActivo(token);
        boolean isValidMatch = jwtService.isTokenValid(token, user, ip);

        // Scenario 2: Active token is null
        user.setTokenActivo(null);
        boolean isValidNull = jwtService.isTokenValid(token, user, ip);

        // THEN
        assertTrue(isValidMatch);
        assertTrue(isValidNull);
    }
}
