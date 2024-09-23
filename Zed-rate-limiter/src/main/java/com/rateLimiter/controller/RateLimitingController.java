package com.rateLimiter.controller;

import com.rateLimiter.model.OtpRequest;  // Import OtpRequest class
import com.rateLimiter.service.RateLimitingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/otp")
public class RateLimitingController {

    @Autowired
    private RateLimitingService rateLimitingService;

    @PostMapping("/send")
    public ResponseEntity<String> sendOtp(@RequestBody OtpRequest otpRequest, @RequestHeader Map<String, String> headers) {
        // Process the OTP request using the service

        System.out.println("Headers: " + headers);
        boolean isBlocked = rateLimitingService.processOtpRequest(otpRequest, headers);
        if (isBlocked) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Carrier or country is blocked");
        }
        return ResponseEntity.ok("OTP Sent");
    }

    @GetMapping("/ping")
    public String testEndpoint() {
        return "Application is running!";
    }
}
