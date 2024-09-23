package com.rateLimiter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

@Service
public class CloudWatchService {

    @Autowired
    private CloudWatchClient cloudWatchClient;

    public void logActionToCloudWatch(String countryCode, String action) {
        // Define the metric
        MetricDatum datum = MetricDatum.builder()
                .metricName("RequestAction")
                .unit(StandardUnit.COUNT)
                .value(1.0)
                .dimensions(d -> d.name("CountryCode").value(countryCode),
                        d -> d.name("Action").value(action)) // Blocked or Allowed
                .build();

        // Create request to send to CloudWatch
        PutMetricDataRequest request = PutMetricDataRequest.builder()
                .namespace("RateLimiter")
                .metricData(datum)
                .build();

        // Send the request
        PutMetricDataResponse response = cloudWatchClient.putMetricData(request);
        System.out.println("Logged metric to CloudWatch: " + response.sdkHttpResponse().statusCode());
    }
}
