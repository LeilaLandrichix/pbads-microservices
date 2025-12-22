package com.pbdas.inference.kafka.producer;

import com.pbdas.inference.model.dto.AnomalyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class AnomalyEventProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(AnomalyEventProducer.class);
    
    @Autowired
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;
    
    @Value("${kafka.topics.anomaly-detected:anomaly.detected.events}")
    private String anomalyDetectedTopic;
    
    public void sendAnomalyDetectedEvent(String userId, LocalDate date, AnomalyResult result) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("userId", userId);
            event.put("date", date.toString());
            event.put("anomalyScore", result.getAnomalyScore());
            event.put("anomalyLevel", result.getLevel().name());
            event.put("message", result.getMessage());
            event.put("contributingFactors", result.getContributingFactors());
            event.put("timestamp", System.currentTimeMillis());
            
            CompletableFuture<SendResult<String, Map<String, Object>>> future = 
                kafkaTemplate.send(anomalyDetectedTopic, userId, event);
            
            future.whenComplete((sendResult, exception) -> {
                if (exception == null) {
                    logger.info("Sent anomaly detected event for user: {} with score: {}",
                        userId, result.getAnomalyScore());
                } else {
                    logger.error("Failed to send anomaly detected event for user: {}",
                        userId, exception);
                }
            });
        } catch (Exception e) {
            logger.error("Error sending anomaly detected event", e);
        }
    }
}
