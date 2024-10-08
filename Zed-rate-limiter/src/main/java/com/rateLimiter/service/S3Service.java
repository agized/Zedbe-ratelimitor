package com.rateLimiter.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;



    // Fetch the blocked carriers from S3
    public List<String> getBlockedCarriers(String bucketName, String key) {
        return fetchListFromS3(bucketName, key);
    }

    // Fetch the blocked countries from S3
    public List<String> getBlockedCountries(String bucketName, String key) {
        return fetchListFromS3(bucketName, key);
    }

    // General method to fetch a list from S3
    private List<String> fetchListFromS3(String bucketName, String key) {
        List<String> list = new ArrayList<>();
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            InputStream objectData = s3Client.getObject(request);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(objectData))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    list.add(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
