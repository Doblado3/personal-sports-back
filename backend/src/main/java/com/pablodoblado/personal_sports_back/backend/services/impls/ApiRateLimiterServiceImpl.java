package com.pablodoblado.personal_sports_back.backend.services.impls;


import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.pablodoblado.personal_sports_back.backend.services.ApiRateLimiterService;

import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class ApiRateLimiterServiceImpl implements ApiRateLimiterService {

    private final Clock clock;

    private final ConcurrentHashMap<String, AtomicInteger> shortTermUsage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> longTermUsage = new ConcurrentHashMap<>();

    private static final int STRAVA_SHORT_TERM_LIMIT = 200;
    private static final int STRAVA_LONG_TERM_LIMIT = 1000;
    private static final long STRAVA_SHORT_TERM_WINDOW = 15 * 60; // 15 minutes in seconds
    private static final long STRAVA_DAILY_WINDOW = 24 * 60 * 60; // 24 hours in seconds

    private static final int AEMET_LIMIT = 50;
    private static final long AEMET_WINDOW_SECONDS = 60; // 1 minute in seconds
    private final ConcurrentHashMap<String, AtomicInteger> aemetUsage = new ConcurrentHashMap<>();

    public ApiRateLimiterServiceImpl() {
        this.clock = Clock.systemUTC();
    }

    public ApiRateLimiterServiceImpl(Clock clock) {
        this.clock = clock;
    }

    public void checkStravaRateLimit() {
        long nowSeconds = clock.instant().getEpochSecond();
        String currentWindowKey = String.valueOf(nowSeconds / STRAVA_SHORT_TERM_WINDOW);
        String currentDayKey = String.valueOf(nowSeconds / STRAVA_DAILY_WINDOW);

        // Clean up old windows
        shortTermUsage.keySet().removeIf(key -> Long.parseLong(key) < Long.parseLong(currentWindowKey));
        longTermUsage.keySet().removeIf(key -> Long.parseLong(key) < Long.parseLong(currentDayKey));

        if (shortTermUsage.computeIfAbsent(currentWindowKey, k -> new AtomicInteger(0)).get() >= STRAVA_SHORT_TERM_LIMIT) {
            throw new RuntimeException("Strava API rate limit exceeded for the 15-minute window.");
        }
        if (longTermUsage.computeIfAbsent(currentDayKey, k -> new AtomicInteger(0)).get() >= STRAVA_LONG_TERM_LIMIT) {
            throw new RuntimeException("Strava API rate limit exceeded for the daily window.");
        }
    }

    public void updateStravaRateLimit(HttpHeaders headers) {
        String usageHeader = headers.getFirst("X-RateLimit-Usage");
        if (usageHeader != null) {
            try {
                String[] usages = usageHeader.split(",");
                if (usages.length == 2) {
                    long nowSeconds = clock.instant().getEpochSecond();
                    String currentWindowKey = String.valueOf(nowSeconds / STRAVA_SHORT_TERM_WINDOW);
                    String currentDayKey = String.valueOf(nowSeconds / STRAVA_DAILY_WINDOW);

                    shortTermUsage.computeIfAbsent(currentWindowKey, k -> new AtomicInteger(0)).set(Integer.parseInt(usages[0].trim()));
                    longTermUsage.computeIfAbsent(currentDayKey, k -> new AtomicInteger(0)).set(Integer.parseInt(usages[1].trim()));
                }
            } catch (Exception e) {
                log.error("Failed to parse Strava rate limit usage header: '{}'", usageHeader, e);
            }
        }
    }

    public void checkAemetRateLimit() {
        long nowSeconds = clock.instant().getEpochSecond();
        String currentWindow = String.valueOf(nowSeconds / AEMET_WINDOW_SECONDS);

        
        aemetUsage.keySet().removeIf(key -> Long.parseLong(key) < Long.parseLong(currentWindow));

        AtomicInteger usage = aemetUsage.computeIfAbsent(currentWindow, k -> new AtomicInteger(0));

        if (usage.get() >= AEMET_LIMIT) {
            long secondsToWait = AEMET_WINDOW_SECONDS - (nowSeconds % AEMET_WINDOW_SECONDS);
            log.warn("Aemet API rate limit reached. Delaying next request by {} seconds.", secondsToWait);
            try {
                
                Thread.sleep(secondsToWait * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Aemet rate limit delay was interrupted", e);
            }
        }
        usage.incrementAndGet();
    }
}