package com.pablodoblado.personal_sports_back.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ApiRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(ApiRateLimiter.class);

    private final ConcurrentHashMap<String, AtomicInteger> shortTermUsage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> longTermUsage = new ConcurrentHashMap<>();

    private static final int STRAVA_SHORT_TERM_LIMIT = 200;
    private static final int STRAVA_LONG_TERM_LIMIT = 1000;
    private static final long STRAVA_SHORT_TERM_WINDOW = 15 * 60 * 1000; 

    private static final int AEMET_LIMIT = 50;
    private static final long AEMET_WINDOW = 60 * 1000; // 1 minute in milliseconds
    private final ConcurrentHashMap<String, AtomicInteger> aemetUsage = new ConcurrentHashMap<>(); 

    public void checkStravaRateLimit() {
    	
        long now = System.currentTimeMillis();
        String currentDay = String.valueOf(now / (24 * 60 * 60 * 1000));
        String currentWindow = String.valueOf(now / STRAVA_SHORT_TERM_WINDOW);

        shortTermUsage.computeIfAbsent(currentWindow, k -> new AtomicInteger(0));
        longTermUsage.computeIfAbsent(currentDay, k -> new AtomicInteger(0));

        if (shortTermUsage.get(currentWindow).get() >= STRAVA_SHORT_TERM_LIMIT) {
            throw new RuntimeException("Strava API rate limit exceeded for the 15-minute window.");
        }
        if (longTermUsage.get(currentDay).get() >= STRAVA_LONG_TERM_LIMIT) {
            throw new RuntimeException("Strava API rate limit exceeded for the daily window.");
        }
    }

    public void updateStravaRateLimit(HttpHeaders headers) {
    	
        long now = System.currentTimeMillis();
        String currentDay = String.valueOf(now / (24 * 60 * 60 * 1000));
        String currentWindow = String.valueOf(now / STRAVA_SHORT_TERM_WINDOW);

        String shortTermUsageHeader = headers.getFirst("X-RateLimit-Usage-Short");
        String longTermUsageHeader = headers.getFirst("X-RateLimit-Usage-Long");

        if (shortTermUsageHeader != null) {
            shortTermUsage.computeIfAbsent(currentWindow, k -> new AtomicInteger(0)).set(Integer.parseInt(shortTermUsageHeader.split(",")[0]));
        }
        if (longTermUsageHeader != null) {
            longTermUsage.computeIfAbsent(currentDay, k -> new AtomicInteger(0)).set(Integer.parseInt(longTermUsageHeader.split(",")[0]));
        }
    }

    public void checkAemetRateLimit() {
        long now = System.currentTimeMillis();
        String currentWindow = String.valueOf(now / AEMET_WINDOW);
        aemetUsage.computeIfAbsent(currentWindow, k -> new AtomicInteger(0));
        if (aemetUsage.get(currentWindow).incrementAndGet() > AEMET_LIMIT) {
        	log.warn("Aemet API rate limit reached");
            throw new RuntimeException("Aemet API rate limit exceeded for the minute window.");
        }
    }
}
