package gdgoc.everyclub.auth.service;

import gdgoc.everyclub.common.exception.ApiException;
import gdgoc.everyclub.common.exception.RateErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter for OTP endpoints.
 * Tracks requests per IP address using a sliding window approach.
 */
@Slf4j
@Service
public class RateLimiterService {

    private final int maxRequests;
    private final int windowSeconds;

    private final ConcurrentHashMap<String, RateLimitRecord> rateLimitMap = new ConcurrentHashMap<>();

    public RateLimiterService(
            @Value("${otp.rate-limit.max-requests:5}") int maxRequests,
            @Value("${otp.rate-limit.window-seconds:300}") int windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    /**
     * Check if the request should be rate limited.
     *
     * @param key The unique identifier (typically IP address)
     * @return true if the request is allowed, false if rate limited
     */
    public boolean tryAcquire(String key) {
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSeconds * 1000L);

        RateLimitRecord record = rateLimitMap.get(key);

        if (record == null || record.windowStart < windowStart) {
            // New window or expired window
            rateLimitMap.put(key, new RateLimitRecord(now, new AtomicInteger(1)));
            return true;
        }

        // Check if we've exceeded the limit
        if (record.count.get() >= maxRequests) {
            log.warn("Rate limit exceeded for key: {}", key);
            return false;
        }

        // Increment the counter
        record.count.incrementAndGet();
        return true;
    }

    /**
     * Acquire a rate limit slot or throw an exception if limit exceeded.
     *
     * @param key The unique identifier (typically IP address)
     * @throws ApiException if rate limit is exceeded
     */
    public void acquireOrThrow(String key) {
        if (!tryAcquire(key)) {
            throw new ApiException(RateErrorCode.RATE_LIMIT_EXCEEDED);
        }
    }

    /**
     * Record class to track rate limit state.
     */
    private static class RateLimitRecord {
        final long windowStart;
        final AtomicInteger count;

        RateLimitRecord(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}