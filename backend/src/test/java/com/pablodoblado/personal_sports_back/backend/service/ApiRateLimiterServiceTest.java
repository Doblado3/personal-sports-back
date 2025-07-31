package com.pablodoblado.personal_sports_back.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiRateLimiterServiceTest {

    private ApiRateLimiterService apiRateLimiter;
    private long stravaShortTermWindow;
    private long stravaDailyWindow;
    private long aemetWindow;

    @BeforeEach
    void setUp() throws Exception {
        apiRateLimiter = new ApiRateLimiterService();
        // Use reflection to get private static final fields
        Field shortTermWindowField = ApiRateLimiterService.class.getDeclaredField("STRAVA_SHORT_TERM_WINDOW");
        shortTermWindowField.setAccessible(true);
        stravaShortTermWindow = (long) shortTermWindowField.get(null);

        Field dailyWindowField = ApiRateLimiterService.class.getDeclaredField("STRAVA_DAILY_WINDOW");
        dailyWindowField.setAccessible(true);
        stravaDailyWindow = (long) dailyWindowField.get(null);

        Field aemetWindowField = ApiRateLimiterService.class.getDeclaredField("AEMET_WINDOW_SECONDS");
        aemetWindowField.setAccessible(true);
        aemetWindow = (long) aemetWindowField.get(null);
    }

    @Test
    void shouldNotThrowExceptionWhenStravaRateLimitIsNotExceeded() {
        StepVerifier.create(apiRateLimiter.checkStravaRateLimit())
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionWhenShortTermRateLimitIsExceeded() throws Exception {
        // Set short-term usage to the limit, long-term is fine
        setStravaUsage(200, 500);

        StepVerifier.create(apiRateLimiter.checkStravaRateLimit())
                .expectErrorMessage("Strava API rate limit exceeded for the 15-minute window.")
                .verify();
    }

    @Test
    void shouldThrowExceptionWhenLongTermRateLimitIsExceeded() throws Exception {
        // Set long-term usage to the limit, short-term is fine
        setStravaUsage(100, 1000);

        StepVerifier.create(apiRateLimiter.checkStravaRateLimit())
                .expectErrorMessage("Strava API rate limit exceeded for the daily window.")
                .verify();
    }

    @Test
    void shouldDelayWhenAemetRateLimitIsExceeded() throws Exception {
        // Set AEMET usage to the limit
        setAemetUsage(50);

        // The mono should complete after a delay, not error out.
        StepVerifier.create(apiRateLimiter.checkAemetRateLimit())
                .expectComplete()
                .verify(Duration.ofSeconds(aemetWindow + 2)); // Verify it takes time to complete
    }

    // Helper methods to set internal state via reflection
    private void setStravaUsage(int shortTerm, int longTerm) throws Exception {
        Field shortTermUsageField = ApiRateLimiterService.class.getDeclaredField("shortTermUsage");
        shortTermUsageField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, AtomicInteger> shortTermUsageMap = (ConcurrentHashMap<String, AtomicInteger>) shortTermUsageField.get(apiRateLimiter);
        String currentWindowKey = String.valueOf((System.currentTimeMillis() / 1000) / stravaShortTermWindow);
        shortTermUsageMap.put(currentWindowKey, new AtomicInteger(shortTerm));

        Field longTermUsageField = ApiRateLimiterService.class.getDeclaredField("longTermUsage");
        longTermUsageField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, AtomicInteger> longTermUsageMap = (ConcurrentHashMap<String, AtomicInteger>) longTermUsageField.get(apiRateLimiter);
        String currentDayKey = String.valueOf((System.currentTimeMillis() / 1000) / stravaDailyWindow);
        longTermUsageMap.put(currentDayKey, new AtomicInteger(longTerm));
    }

    private void setAemetUsage(int usage) throws Exception {
        Field aemetUsageField = ApiRateLimiterService.class.getDeclaredField("aemetUsage");
        aemetUsageField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, AtomicInteger> aemetUsageMap = (ConcurrentHashMap<String, AtomicInteger>) aemetUsageField.get(apiRateLimiter);
        String currentWindow = String.valueOf((System.currentTimeMillis() / 1000) / aemetWindow);
        aemetUsageMap.put(currentWindow, new AtomicInteger(usage));
    }
}