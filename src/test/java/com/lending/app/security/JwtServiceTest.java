package com.lending.app.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // 64+ length secret for HS256
        String secret = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        long expiry = 3600_000L;
        jwtService = new JwtService(secret, expiry);
    }

    @Test
    void generateToken_and_extractClaims_work() {
        String token = jwtService.generateToken("alice", Map.of("uid", "01HXYZ"));

        String username = jwtService.extractUsername(token);
        String uid = jwtService.extractClaim(token, claims -> claims.get("uid", String.class));

        assertThat(username).isEqualTo("alice");
        assertThat(uid).isEqualTo("01HXYZ");
    }
}


