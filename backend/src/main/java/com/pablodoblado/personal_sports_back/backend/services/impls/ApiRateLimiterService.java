package com.pablodoblado.personal_sports_back.backend.services.impls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ApiRateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(ApiRateLimiterService.class);

    private final ConcurrentHashMap<String, AtomicInteger> shortTermUsage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> longTermUsage = new ConcurrentHashMap<>();

    private static final int STRAVA_SHORT_TERM_LIMIT = 200;
    private static final int STRAVA_LONG_TERM_LIMIT = 1000;
    private static final long STRAVA_SHORT_TERM_WINDOW = 15 * 60; // 15 minutes in seconds
    private static final long STRAVA_DAILY_WINDOW = 24 * 60 * 60; // 24 hours in seconds

    private static final int AEMET_LIMIT = 50;
    private static final long AEMET_WINDOW_SECONDS = 60; // 1 minute in seconds
    private final ConcurrentHashMap<String, AtomicInteger> aemetUsage = new ConcurrentHashMap<>();

    public Mono<Void> checkStravaRateLimit() {
        return Mono.fromRunnable(() -> {
            long nowSeconds = System.currentTimeMillis() / 1000;
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
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public void updateStravaRateLimit(HttpHeaders headers) {
        String usageHeader = headers.getFirst("X-RateLimit-Usage");
        if (usageHeader != null) {
            try {
                String[] usages = usageHeader.split(",");
                if (usages.length == 2) {
                    long nowSeconds = System.currentTimeMillis() / 1000;
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

    public Mono<Void> checkAemetRateLimit() {
        return Mono.defer(() -> {
            long nowSeconds = System.currentTimeMillis() / 1000;
            String currentWindow = String.valueOf(nowSeconds / AEMET_WINDOW_SECONDS);

            // Clean up old windows
            aemetUsage.keySet().removeIf(key -> Long.parseLong(key) < Long.parseLong(currentWindow));

            AtomicInteger usage = aemetUsage.computeIfAbsent(currentWindow, k -> new AtomicInteger(0));

            if (usage.get() >= AEMET_LIMIT) {
                long secondsToWait = AEMET_WINDOW_SECONDS - (nowSeconds % AEMET_WINDOW_SECONDS);
                log.warn("Aemet API rate limit reached. Delaying next request by {} seconds.", secondsToWait);
                return Mono.delay(Duration.ofSeconds(secondsToWait)).then();
            }

            usage.incrementAndGet();
            return Mono.empty();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
