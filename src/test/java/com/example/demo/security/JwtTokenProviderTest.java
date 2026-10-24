package com.example.demo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(
                "SuperSecretFarmVetKey2026WithAtLeast256BitsLengthForHmacSha256Security!",
                3600000L,
                new ObjectMapper()
        );
    }

    @Test
    void testTokenGenerationAndValidation() {
        String token = tokenProvider.generateTokenForUser(42L, "ramesh_farmer", "ramesh@farm.in", "ROLE_FARMER");
        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("ramesh_farmer", tokenProvider.getUsernameFromJWT(token));
        assertEquals(42L, tokenProvider.getUserIdFromJWT(token));
        assertEquals("ROLE_FARMER", tokenProvider.getRoleFromJWT(token));
    }

    @Test
    void testTamperedTokenFailsValidation() {
        String token = tokenProvider.generateTokenForUser(42L, "ramesh_farmer", "ramesh@farm.in", "ROLE_FARMER");
        String tamperedToken = token.substring(0, token.length() - 4) + "XXXX";
        assertFalse(tokenProvider.validateToken(tamperedToken));
    }
}
