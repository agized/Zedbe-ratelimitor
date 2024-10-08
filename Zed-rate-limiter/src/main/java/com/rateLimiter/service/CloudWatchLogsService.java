package com.rateLimiter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceAlreadyExistsException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class CloudWatchLogsService {

    @Autowired
    private CloudWatchLogsClient cloudWatchLogsClient;

    private final String logGroupName = "Zedbe-ratelimiting-logs";  // Specify your log group name here
    private final String logStreamName = "/zed/ratelimitingaction";            // Specify your log stream name here

    public void logAction(String message) {
        try {
            // Ensure the log group exists, create if necessary
            //createLogGroupIfNotExists();

            // Ensure the log stream exists, create if necessary
            //createLogStreamIfNotExists();

            // Log the message
            logMessageToCloudWatch(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createLogGroupIfNotExists() {
        try {
            cloudWatchLogsClient.createLogGroup(CreateLogGroupRequest.builder().logGroupName(logGroupName).build());
        } catch (ResourceAlreadyExistsException e) {
            // Log group already exists, nothing to do
        }
    }

    private void createLogStreamIfNotExists() {
        try {
            cloudWatchLogsClient.createLogStream(CreateLogStreamRequest.builder()
                    .logGroupName(logGroupName)
                    .logStreamName(logStreamName)
                    .build());
        } catch (ResourceAlreadyExistsException e) {
            // Log stream already exists, nothing to do
        }
    }

    private void logMessageToCloudWatch(String message) {
        // Find the next sequence token for the log stream
        DescribeLogStreamsResponse logStreamsResponse = cloudWatchLogsClient.describeLogStreams(DescribeLogStreamsRequest.builder()
                .logGroupName(logGroupName)
                .logStreamNamePrefix(logStreamName)
                .build());

        String sequenceToken = logStreamsResponse.logStreams().get(0).uploadSequenceToken();

        // Create a log event
        InputLogEvent logEvent = InputLogEvent.builder()
                .message(message)
                .timestamp(Instant.now().toEpochMilli())  // Current timestamp
                .build();

        // Put log event into CloudWatch
        PutLogEventsRequest putLogEventsRequest = PutLogEventsRequest.builder()
                .logGroupName(logGroupName)
                .logStreamName(logStreamName)
                .logEvents(Collections.singletonList(logEvent))
                .sequenceToken(sequenceToken)
                .build();

        cloudWatchLogsClient.putLogEvents(putLogEventsRequest);
    }
}
