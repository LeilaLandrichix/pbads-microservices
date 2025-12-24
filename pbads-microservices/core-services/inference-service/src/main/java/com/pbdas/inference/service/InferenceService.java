package com.pbdas.inference.service;

import com.pbdas.data.model.event.DataLoggedEvent;
import com.pbdas.inference.model.dto.AnomalyResult;
import com.pbdas.inference.kafka.producer.AnomalyEventProducer;
import com.pbdas.inference.client.RecommendationServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InferenceService {
    
    private static final Logger logger = LoggerFactory.getLogger(InferenceService.class);
    
    @Autowired
    private AnomalyDetectorService anomalyDetectorService;
    
    @Autowired
    private AnomalyEventProducer anomalyEventProducer;
    
    @Autowired
    private AlertServiceClient alertServiceClient;
    
    @Autowired
    private RecommendationServiceClient recommendationServiceClient;
    
    @Value("${inference.model.default-threshold:0.5}")
    private double defaultThreshold;
    
    public void processDataForAnomalyDetection(DataLoggedEvent event) {
        try {
            logger.info("Processing data for anomaly detection - User: {}, Date: {}", 
                event.getUserId(), event.getDate());
            
            // Prepare feature map from event
            Map<String, Object> features = new HashMap<>();
            features.put("sleepHours", event.getSleepHours());
            features.put("steps", event.getSteps());
            features.put("caloriesBurned", event.getCaloriesBurned());
            features.put("waterIntakeMl", event.getWaterIntakeMl());
            features.put("studyHours", event.getStudyHours());
            features.put("moodScore", event.getMoodScore());
            
            logger.info("Features extracted: {}", features);
            
            // Run anomaly detection
            AnomalyResult result = anomalyDetectorService.detectAnomaly(
                event.getUserId(), features, defaultThreshold);
            
            logger.info("Anomaly detection result - isAnomaly: {}, score: {}, level: {}, message: {}", 
                result.isAnomaly(), result.getAnomalyScore(), result.getLevel(), result.getMessage());
            
            // If anomaly detected, publish alert event
            if (result.isAnomaly()) {
                logger.warn("*** ANOMALY DETECTED *** User: {}, Score: {}, Level: {}, Message: {}",
                    event.getUserId(), result.getAnomalyScore(), result.getLevel(), result.getMessage());
                
                // Get AI recommendation for this alert
                String recommendation = null;
                try {
                    logger.info("Requesting AI recommendation for user: {}", event.getUserId());
                    recommendation = recommendationServiceClient.getRecommendationForAlert(
                        event.getUserId(), event, result);
                    if (recommendation != null && !recommendation.isEmpty()) {
                        logger.info("✓ Received AI recommendation for alert");
                    } else {
                        logger.warn("No recommendation received from recommendation service");
                    }
                } catch (Exception e) {
                    logger.error("Failed to get recommendation, continuing with alert creation", e);
                    // Don't fail alert creation if recommendation fails
                }
                
                // ALWAYS try direct HTTP call first (more reliable than Kafka)
                logger.info("Creating alert directly via HTTP for user: {}", event.getUserId());
                alertServiceClient.createAlertDirectly(
                    event.getUserId(),
                    event.getDate(),
                    result,
                    recommendation
                );
                logger.info("✓ Alert created directly via HTTP");
                
                // Also try Kafka (non-blocking, won't fail if Kafka is down)
                try {
                    anomalyEventProducer.sendAnomalyDetectedEvent(
                        event.getUserId(),
                        event.getDate(),
                        result
                    );
                    logger.info("Anomaly event also sent via Kafka (backup)");
                } catch (Exception e) {
                    logger.debug("Kafka send failed (non-critical): {}", e.getMessage());
                }
            } else {
                logger.info("No anomaly detected for user: {} with score: {}",
                    event.getUserId(), result.getAnomalyScore());
            }
            
        } catch (Exception e) {
            logger.error("Error processing data for anomaly detection", e);
            e.printStackTrace();
        }
    }
    
    public AnomalyResult detectAnomaly(String userId, Map<String, Object> features) {
        return anomalyDetectorService.detectAnomaly(userId, features, defaultThreshold);
    }
}
