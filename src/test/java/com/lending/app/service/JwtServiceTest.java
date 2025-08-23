package com.lending.app.service;

import com.lending.app.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService Tests")
class JwtServiceTest {

    private static final String SECRET = "01234567890123456789012345678901";
    private static final long EXPIRATION_MS = 3600000;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MS);
    }

    @Test
    @DisplayName("should generate token and extract username correctly")
    void shouldGenerateTokenAndExtractUsername() {
        String subject = "user1";
        Map<String, Object> claims = Map.of("test", "test");

        String token = jwtService.generateToken(subject, claims);

        String extractedUsername = jwtService.extractUsername(token);
        assertThat(extractedUsername).isEqualTo(subject);

        String extractedRole = jwtService.extractClaim(token, c -> c.get("test", String.class));
        assertThat(extractedRole).isEqualTo("test");
    }

    @Test
    @DisplayName("should extract claims correctly")
    void shouldExtractClaims() {
        String subject = "user1";
        Map<String, Object> claims = Map.of("test", "test");

        String token = jwtService.generateToken(subject, claims);

        String department = jwtService.extractClaim(token, c -> c.get("test", String.class));
        assertThat(department).isEqualTo("test");
    }
}
