package com.rateLimiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RateLimitingApp {
    public static void main(String[] args) {
        SpringApplication.run(RateLimitingApp.class, args);
    }
}
