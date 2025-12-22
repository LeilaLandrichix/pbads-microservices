package com.pbdas.data.service;

import com.pbdas.data.model.event.DataLoggedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InferenceServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(InferenceServiceClient.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${inference.service.url:http://localhost:8084}")
    private String inferenceServiceUrl;
    
    @Value("${data.service.direct-inference:true}")
    private boolean directInferenceEnabled;
    
    public InferenceServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public void processDataEventDirectly(DataLoggedEvent event) {
        if (!directInferenceEnabled) {
            logger.debug("Direct inference disabled, skipping HTTP call");
            return;
        }
        
        try {
            String url = inferenceServiceUrl + "/api/inference/process-event";
            logger.info("Calling inference service directly at: {}", url);
            logger.info("Event data - User: {}, Date: {}, Sleep: {}, Steps: {}, Mood: {}", 
                event.getUserId(), event.getDate(), event.getSleepHours(), 
                event.getSteps(), event.getMoodScore());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<DataLoggedEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<?> response = restTemplate.postForEntity(url, entity, Object.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("✓ Successfully processed data event directly via HTTP for user: {}", event.getUserId());
            } else {
                logger.warn("✗ Failed to process data event directly, status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("✗✗✗ Error calling inference service directly: {}", e.getMessage(), e);
            e.printStackTrace();
        }
    }
}

