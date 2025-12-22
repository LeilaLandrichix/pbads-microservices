package com.pbdas.inference.service;

import com.pbdas.inference.model.dto.AnomalyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class AlertServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertServiceClient.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${alert.service.url:http://localhost:8085}")
    private String alertServiceUrl;
    
    @Value("${inference.service.direct-alert:true}")
    private boolean directAlertEnabled;
    
    public AlertServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public void createAlertDirectly(String userId, LocalDate date, AnomalyResult result) {
        if (!directAlertEnabled) {
            return;
        }
        
        try {
            String url = alertServiceUrl + "/api/alerts/create-direct";
            
            Map<String, Object> request = new HashMap<>();
            request.put("userId", userId);
            request.put("date", date.toString());
            request.put("anomalyScore", result.getAnomalyScore());
            request.put("anomalyLevel", result.getLevel().name());
            request.put("message", result.getMessage());
            request.put("contributingFactors", result.getContributingFactors());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<?> response = restTemplate.postForEntity(url, entity, Object.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Successfully created alert directly via HTTP for user: {}", userId);
            } else {
                logger.warn("Failed to create alert directly, status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error calling alert service directly", e);
        }
    }
}

