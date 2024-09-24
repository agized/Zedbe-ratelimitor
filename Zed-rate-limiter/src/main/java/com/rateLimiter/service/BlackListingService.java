package com.rateLimiter.service;

import com.rateLimiter.model.OtpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class BlackListingService {

    @Autowired
    private S3Service s3Service;
    @Autowired
    private CloudWatchLogsService cloudWatchLogsService;

    private final String bucketName = "testratelimitng";
    private final String blockedCarriersKey = "blocked_carriers.txt";
    private final String blockedCountriesKey = "blocked_countries.txt";

    // Process the OTP request and determine if it should be blocked
    public boolean processOtpRequest(OtpRequest otpRequest, Map<String, String> headers) {

        String carrier = headers.get("getcarrier");
        String countryCode = otpRequest.getMobile().substring(0, 3);
        List<String> blockedCarriers = s3Service.getBlockedCarriers(bucketName, blockedCarriersKey);
        List<String> blockedCountries = s3Service.getBlockedCountries(bucketName, blockedCountriesKey);

        // Check if the carrier or country is blocked
        if (blockedCarriers.contains(carrier) || blockedCountries.contains(countryCode)) {
            publishAction("BLOCKED", otpRequest, headers, countryCode);
            return true;
        }

        publishAction("ALLOWED", otpRequest, headers,countryCode);
        return false;
    }

    private void publishAction(String action, OtpRequest otpRequest, Map<String, String> headers, String countryCode) {
        String requestDetails = String.format("Mobile: %s, UserType: %d", otpRequest.getMobile(), otpRequest.getUserType());

        // Build the headers string
        StringBuilder headersBuilder = new StringBuilder();
        headers.forEach((key, value) -> headersBuilder.append(key).append(": ").append(value).append(", "));

        // Log the action with full request details and headers
        String logMessage = String.format(
                "Action: %s, Request: [%s], Headers: [%s], Country: %s",
                action,
                requestDetails,
                headersBuilder.toString(),
                countryCode
        );

        // Log to CloudWatch
        cloudWatchLogsService.logAction(logMessage);
        System.out.println(action + " action for mobile: " + otpRequest.getMobile() + " and carrier: " + headers.get("getcarrier"));
    }
}
