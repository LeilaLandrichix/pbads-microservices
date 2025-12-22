package com.pbdas.alert.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbdas.alert.model.entity.Alert;
import com.pbdas.alert.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AnomalyEventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(AnomalyEventConsumer.class);
    
    @Autowired
    private AlertService alertService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @KafkaListener(
        topics = "${kafka.topics.anomaly-detected:anomaly.detected.events}",
        groupId = "${spring.kafka.consumer.group-id:alert-service-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAnomalyDetectedEvent(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {
        
        try {
            logger.warn("🔔 Received anomaly detected event from Kafka - Key: {}, Event: {}", key, event);
            
            // Extract event data
            String userId = (String) event.get("userId");
            String date = (String) event.get("date");
            Double anomalyScore = getDoubleValue(event.get("anomalyScore"));
            String anomalyLevel = (String) event.get("anomalyLevel");
            String message = (String) event.get("message");
            
            logger.info("Extracted data - userId: {}, date: {}, score: {}, level: {}, message: {}", 
                userId, date, anomalyScore, anomalyLevel, message);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> contributingFactors = (Map<String, Object>) event.get("contributingFactors");
            
            if (userId == null || date == null || anomalyScore == null) {
                logger.error("❌ Invalid anomaly event data: userId={}, date={}, score={}, fullEvent={}", 
                    userId, date, anomalyScore, event);
                return;
            }
            
            // Create alert
            logger.info("Creating alert for user: {} on date: {} with score: {}", userId, date, anomalyScore);
            Alert alert = alertService.createAlert(
                userId,
                date,
                anomalyScore,
                anomalyLevel != null ? anomalyLevel : "MEDIUM",
                message != null ? message : "Anomaly detected",
                contributingFactors
            );
            
            logger.warn("✅ SUCCESS: Alert created! ID: {}, User: {}, Date: {}, Score: {}, Level: {}", 
                alert.getId(), userId, date, anomalyScore, anomalyLevel);
            
            // Acknowledge the message
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                logger.info("Kafka message acknowledged");
            }
            
        } catch (Exception e) {
            logger.error("❌❌❌ ERROR processing anomaly detected event: {}", e.getMessage(), e);
            e.printStackTrace();
            // In production, you might want to send to a dead letter queue
        }
    }
    
    private Double getDoubleValue(Object value) {
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

