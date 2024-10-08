package com.rateLimiter.controller;

import com.rateLimiter.model.OtpRequest;
import com.rateLimiter.service.BlackListingService;
import com.rateLimiter.service.RateLimitingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class RateLimitingController {

    @Autowired
    private BlackListingService blackListingService;

    @Autowired
    private RateLimitingService rateLimitingService;

    @PostMapping("/verify")
    public ResponseEntity<String> sendOtp(@RequestBody OtpRequest otpRequest, @RequestHeader Map<String, String> headers) {

        boolean isBlocked = blackListingService.processOtpRequest(otpRequest, headers);

        if (isBlocked) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Carrier or country is blocked");
        }

//        boolean isRateLimitExceeded = !rateLimitingService.isAllowed(otpRequest.getMobile());
//
//         if (isRateLimitExceeded) {
//             // Return 429 Too Many Requests if the rate limit is exceeded
//             return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded. Try again later.");
//         }
       return ResponseEntity.ok("OTP Sent");
    }

    @GetMapping("/ping")
    public String testEndpoint() {
        return "Application is running!";
    }
}
