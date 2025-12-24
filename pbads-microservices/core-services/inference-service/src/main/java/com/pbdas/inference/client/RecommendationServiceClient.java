package com.pbdas.inference.client;

import com.pbdas.data.model.event.DataLoggedEvent;
import com.pbdas.inference.model.dto.AnomalyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class RecommendationServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceClient.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${recommendation.service.url:http://localhost:8087}")
    private String recommendationServiceUrl;
    
    @Value("${inference.service.direct-recommendation:true}")
    private boolean directRecommendationEnabled;
    
    public RecommendationServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public String getRecommendationForAlert(String userId, DataLoggedEvent event, AnomalyResult anomalyResult) {
        if (!directRecommendationEnabled) {
            logger.debug("Direct recommendation calls are disabled");
            return null;
        }
        
        try {
            // Build data summary for Gemini prompt
            String dataSummary = buildAlertDataSummary(event, anomalyResult);
            
            String url = recommendationServiceUrl + "/api/recommendations/user/" + userId + 
                        "?dataSummary=" + java.net.URLEncoder.encode(dataSummary, "UTF-8");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            logger.info("Requesting recommendation from recommendation service for user: {}", userId);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Object recommendation = body.get("recommendation");
                if (recommendation != null) {
                    logger.info("Successfully received recommendation from recommendation service");
                    return recommendation.toString();
                }
            }
            
            logger.warn("No recommendation received from recommendation service");
            return null;
            
        } catch (Exception e) {
            logger.error("Error calling recommendation service", e);
            return null; // Don't fail alert creation if recommendation fails
        }
    }
    
    private String buildAlertDataSummary(DataLoggedEvent event, AnomalyResult anomalyResult) {
        StringBuilder summary = new StringBuilder();
        
        summary.append("ALERT TRIGGERED - Anomaly Detected\n");
        summary.append("=====================================\n\n");
        
        summary.append("Anomaly Details:\n");
        summary.append(String.format("- Anomaly Score: %.2f\n", anomalyResult.getAnomalyScore()));
        summary.append(String.format("- Severity Level: %s\n", anomalyResult.getLevel()));
        summary.append(String.format("- Message: %s\n\n", anomalyResult.getMessage()));
        
        String dateStr = event.getDate() != null ? event.getDate().toString() : "Unknown";
        summary.append("User's Daily Habits (Date: ").append(dateStr).append("):\n");
        if (event.getSleepHours() != null) {
            summary.append(String.format("- Sleep Hours: %.1f hours\n", event.getSleepHours()));
        }
        if (event.getSteps() != null) {
            summary.append(String.format("- Steps: %d\n", event.getSteps()));
        }
        if (event.getCaloriesBurned() != null) {
            summary.append(String.format("- Calories Burned: %d\n", event.getCaloriesBurned()));
        }
        if (event.getWaterIntakeMl() != null) {
            summary.append(String.format("- Water Intake: %d ml\n", event.getWaterIntakeMl()));
        }
        if (event.getStudyHours() != null) {
            summary.append(String.format("- Study Hours: %.1f hours\n", event.getStudyHours()));
        }
        if (event.getMoodScore() != null) {
            summary.append(String.format("- Mood Score: %d/10\n", event.getMoodScore()));
        }
        
        summary.append("\nContributing Factors:\n");
        if (anomalyResult.getContributingFactors() != null && !anomalyResult.getContributingFactors().isEmpty()) {
            for (Map.Entry<String, Object> factor : anomalyResult.getContributingFactors().entrySet()) {
                summary.append(String.format("- %s: %s\n", factor.getKey(), factor.getValue()));
            }
        }
        
        summary.append("\nPlease provide specific, actionable recommendations to help improve this user's habits and lifestyle based on the detected anomalies.");
        
        return summary.toString();
    }
}

