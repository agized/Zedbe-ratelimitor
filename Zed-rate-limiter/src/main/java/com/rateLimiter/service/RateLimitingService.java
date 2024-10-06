package com.rateLimiter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimitingService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${rate.limit.requests}")
    private int requestLimit;

    @Value("${rate.limit.timewindow}")
    private int timeWindowSeconds;

    public boolean isAllowed(String phoneNumber) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        // Redis key for rate limiting
        String redisKey = "rate_limit_" + phoneNumber;

        // Increment request count and set TTL if the key does not exist
        Long requestCount = ops.increment(redisKey);

        // If this is the first request, set the expiration time to 60 seconds
        if (requestCount == 1) {
            redisTemplate.expire(redisKey, timeWindowSeconds, TimeUnit.SECONDS);
        }

        // Check if the request count has exceeded the limit
        return requestCount <= requestLimit;
    }
}
