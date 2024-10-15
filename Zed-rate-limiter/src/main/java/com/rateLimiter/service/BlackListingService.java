package com.rateLimiter.service;

import com.rateLimiter.model.OtpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BlackListingService {

    @Autowired
    private S3Service s3Service;
    @Autowired
    private CloudWatchLogsService cloudWatchLogsService;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;


    // Process the OTP request and determine if it should be blocked
    public boolean processOtpRequest(OtpRequest otpRequest, Map<String, String> headers) {

        String carrier = headers.get("getcarrier");
        String countryCode = otpRequest.getMobile().substring(0, 3);
        String countryCode2 = otpRequest.getMobile().substring(0, 4);
//        String blockedCarriersKey = "blocked_carriers.txt";
//        List<String> blockedCarriers = s3Service.getBlockedCarriers(bucketName, blockedCarriersKey);
//        String blockedCountriesKey = "blocked_countries.txt";
//        List<String> blockedCountries = s3Service.getBlockedCountries(bucketName, blockedCountriesKey);

        List<String> blockedCarriers = Arrays.asList("KIYVSTAR");
        List<String> blockedCountries = Arrays.asList("+374", "+52","","+54","+504","+374","+381","+966","+63");
        boolean isCountryBlocked= false;
        boolean isCarrierBlocked= false;


        // Check if the carrier or country is blocked
        if (blockedCarriers.contains(carrier)) {
            isCarrierBlocked = true;
            publishAction("BLOCKED", otpRequest, headers, "Carrier Blocked",isCarrierBlocked, isCountryBlocked);
            return true;
        }

        // Check if countryCode is blocked
        if (blockedCountries.contains(countryCode)) {
            isCountryBlocked =true;
            publishAction("BLOCKED", otpRequest, headers, countryCode,isCarrierBlocked, isCountryBlocked);
            return true;
        }

        // Check if countryCode2 is blocked
        if (blockedCountries.contains(countryCode2)) {
            isCountryBlocked =true;
            publishAction("BLOCKED", otpRequest, headers, countryCode2,isCarrierBlocked, isCountryBlocked);
            return true;
        }

        // If none are blocked
        publishAction("ALLOWED", otpRequest, headers, countryCode,isCarrierBlocked,isCountryBlocked);
        return false;
    }

    private void publishAction(String action, OtpRequest otpRequest, Map<String, String> headers, String countryCode, boolean isCarrierBlocked,boolean isCountryBlocked) {
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

        if ("BLOCKED".equals(action)) {
            if (isCountryBlocked) {
                logMessage += ", Reason: Country is blocked";
            } else if (isCarrierBlocked) {
                logMessage += ", Reason: Carrier is blocked";
            }
        }

        // Log to CloudWatch
        cloudWatchLogsService.logAction(logMessage);
        log.info(action + " action for mobile: " + otpRequest.getMobile() + " and carrier: " + headers.get("getcarrier"));
    }
}
