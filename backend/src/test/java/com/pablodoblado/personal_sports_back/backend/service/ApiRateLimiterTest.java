package com.pablodoblado.personal_sports_back.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ApiRateLimiterTest {

	@Autowired
    private ApiRateLimiter apiRateLimiter;

    @BeforeEach
    void setUp() {
        apiRateLimiter = new ApiRateLimiter();
    }

    @Test
    void shouldNotThrowExceptionWhenRateLimitIsNotExceeded() {
        assertDoesNotThrow(() -> apiRateLimiter.checkStravaRateLimit());
    }

    @Test
    void shouldThrowExceptionWhenShortTermRateLimitIsExceeded() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RateLimit-Usage-Short", "200,200");
        apiRateLimiter.updateStravaRateLimit(headers);
        assertThrows(RuntimeException.class, () -> apiRateLimiter.checkStravaRateLimit());
    }

    @Test
    void shouldThrowExceptionWhenLongTermRateLimitIsExceeded() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RateLimit-Usage-Long", "1000,1000");
        apiRateLimiter.updateStravaRateLimit(headers);
        assertThrows(RuntimeException.class, () -> apiRateLimiter.checkStravaRateLimit());
    }

    @Test
    void shouldUpdateRateLimitFromHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RateLimit-Usage-Short", "10,200");
        headers.add("X-RateLimit-Usage-Long", "100,1000");
        apiRateLimiter.updateStravaRateLimit(headers);
        assertDoesNotThrow(() -> apiRateLimiter.checkStravaRateLimit());
    }

    @Test
    void shouldThrowExceptionWhenAemetRateLimitIsExceeded() {
        for (int i = 0; i < 50; i++) {
            assertDoesNotThrow(() -> apiRateLimiter.checkAemetRateLimit());
        }
        assertThrows(RuntimeException.class, () -> apiRateLimiter.checkAemetRateLimit());
    }
}