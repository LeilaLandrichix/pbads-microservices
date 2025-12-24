package com.pbdas.alert.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbdas.alert.model.entity.Alert;
import com.pbdas.alert.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlertService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);
    
    @Autowired
    private AlertRepository alertRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Transactional
    public Alert createAlert(String userId, String date, Double anomalyScore, 
                           String anomalyLevel, String message, Map<String, Object> contributingFactors) {
        return createAlert(userId, date, anomalyScore, anomalyLevel, message, contributingFactors, null);
    }
    
    @Transactional
    public Alert createAlert(String userId, String date, Double anomalyScore, 
                           String anomalyLevel, String message, Map<String, Object> contributingFactors, String recommendation) {
        try {
            Alert alert = new Alert();
            alert.setUserId(userId);
            alert.setDate(date);
            alert.setAnomalyScore(anomalyScore);
            alert.setAnomalyLevel(anomalyLevel);
            alert.setMessage(message);
            alert.setStatus("ACTIVE");
            
            // Convert contributing factors to JSON string
            if (contributingFactors != null && !contributingFactors.isEmpty()) {
                try {
                    alert.setContributingFactors(objectMapper.writeValueAsString(contributingFactors));
                } catch (JsonProcessingException e) {
                    logger.warn("Failed to serialize contributing factors", e);
                    alert.setContributingFactors(contributingFactors.toString());
                }
            }
            
            // Set recommendation if provided
            if (recommendation != null && !recommendation.isEmpty()) {
                alert.setRecommendation(recommendation);
            }
            
            Alert saved = alertRepository.save(alert);
            logger.info("Created alert for user: {} with score: {} and level: {}", 
                userId, anomalyScore, anomalyLevel);
            
            return saved;
        } catch (Exception e) {
            logger.error("Error creating alert for user: {}", userId, e);
            throw e;
        }
    }
    
    public List<Alert> getAlertsByUserId(String userId) {
        return alertRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<Alert> getActiveAlertsByUserId(String userId) {
        return alertRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "ACTIVE");
    }
    
    public List<Alert> getAllActiveAlerts() {
        return alertRepository.findByStatusOrderByCreatedAtDesc("ACTIVE");
    }
    
    @Transactional
    public Alert acknowledgeAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        alert.setStatus("ACKNOWLEDGED");
        return alertRepository.save(alert);
    }
    
    @Transactional
    public Alert resolveAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        alert.setStatus("RESOLVED");
        return alertRepository.save(alert);
    }
    
    public long getActiveAlertCount(String userId) {
        return alertRepository.countByUserIdAndStatus(userId, "ACTIVE");
    }
}
