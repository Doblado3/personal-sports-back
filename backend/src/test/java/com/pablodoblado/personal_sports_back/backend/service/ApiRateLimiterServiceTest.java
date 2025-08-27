package com.pablodoblado.personal_sports_back.backend.service;

import com.pablodoblado.personal_sports_back.backend.services.impls.ApiRateLimiterServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;


public class ApiRateLimiterServiceTest {

    private ApiRateLimiterServiceImpl apiRateLimiter;

    @BeforeEach
    void setUp() {
        apiRateLimiter = new ApiRateLimiterServiceImpl();
    }

    @Test
    void shouldNotThrowExceptionWhenStravaRateLimitIsNotExceeded() {
    	
        assertDoesNotThrow(() -> apiRateLimiter.checkStravaRateLimit());
    }

    @Test
    void shouldThrowExceptionWhenShortTermRateLimitIsExceeded() throws Exception {
        
        setStravaUsage(200, 500);

        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            apiRateLimiter.checkStravaRateLimit();
        });
        assertEquals("Strava API rate limit exceeded for the 15-minute window.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLongTermRateLimitIsExceeded() throws Exception {
        
        setStravaUsage(100, 1000);

        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            apiRateLimiter.checkStravaRateLimit();
        });
        assertEquals("Strava API rate limit exceeded for the daily window.", exception.getMessage());
    }

    @Test
    void shouldNotThrowAndDelayWhenAemetRateLimitIsExceeded() {
        // Define a fixed point in time, 59 seconds into a minute.
        // The AEMET window is 60s, so this leaves 1s to wait.
        Instant fixedInstant = Instant.ofEpochSecond(1672531259L); // An arbitrary time where (seconds % 60) = 59
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);

        // Create a service instance with the fixed clock.
        ApiRateLimiterServiceImpl rateLimiterWithFixedClock = new ApiRateLimiterServiceImpl(fixedClock);

        // Use a helper to set the usage for the specific time window.
        assertDoesNotThrow(() -> setAemetUsageForService(rateLimiterWithFixedClock, 50, fixedClock));

        // Assert that the method completes within 2 seconds. The actual wait time should be 1 second.
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            rateLimiterWithFixedClock.checkAemetRateLimit();
        }, "The delay should be predictable and less than 2 seconds.");
    }

    // Helper methods to set internal state via reflection
    private void setStravaUsage(int shortTerm, int longTerm) throws Exception {
        Field shortTermUsageField = ApiRateLimiterServiceImpl.class.getDeclaredField("shortTermUsage");
        shortTermUsageField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, AtomicInteger> shortTermUsageMap = (ConcurrentHashMap<String, AtomicInteger>) shortTermUsageField.get(apiRateLimiter);
        long stravaShortTermWindow = getPrivateStaticLong("STRAVA_SHORT_TERM_WINDOW");
        String currentWindowKey = String.valueOf((System.currentTimeMillis() / 1000) / stravaShortTermWindow);
        shortTermUsageMap.put(currentWindowKey, new AtomicInteger(shortTerm));

        Field longTermUsageField = ApiRateLimiterServiceImpl.class.getDeclaredField("longTermUsage");
        longTermUsageField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, AtomicInteger> longTermUsageMap = (ConcurrentHashMap<String, AtomicInteger>) longTermUsageField.get(apiRateLimiter);
        long stravaDailyWindow = getPrivateStaticLong("STRAVA_DAILY_WINDOW");
        String currentDayKey = String.valueOf((System.currentTimeMillis() / 1000) / stravaDailyWindow);
        longTermUsageMap.put(currentDayKey, new AtomicInteger(longTerm));
    }

    private void setAemetUsageForService(ApiRateLimiterServiceImpl service, int usage, Clock clock) throws Exception {
        Field aemetUsageField = ApiRateLimiterServiceImpl.class.getDeclaredField("aemetUsage");
        aemetUsageField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, AtomicInteger> aemetUsageMap = (ConcurrentHashMap<String, AtomicInteger>) aemetUsageField.get(service);
        
        long aemetWindow = getPrivateStaticLong("AEMET_WINDOW_SECONDS");
        long nowSeconds = clock.instant().getEpochSecond();
        String currentWindow = String.valueOf(nowSeconds / aemetWindow);
        aemetUsageMap.put(currentWindow, new AtomicInteger(usage));
    }

    private long getPrivateStaticLong(String fieldName) throws Exception {
        Field field = ApiRateLimiterServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (long) field.get(null);
    }
}