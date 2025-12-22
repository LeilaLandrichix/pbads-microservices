package com.pbdas.inference.kafka.consumer;

import com.pbdas.data.model.event.DataLoggedEvent;
import com.pbdas.inference.service.InferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class DataEventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(DataEventConsumer.class);
    
    @Autowired
    private InferenceService inferenceService;
    
    @KafkaListener(
        topics = "${kafka.topics.data-logged:data.logged.events}",
        groupId = "${spring.kafka.consumer.group-id:inference-service-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeDataLoggedEvent(
            @Payload DataLoggedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Received data logged event for user: {} on date: {}",
                event.getUserId(), event.getDate());
            
            // Process the event for anomaly detection
            inferenceService.processDataForAnomalyDetection(event);
            
            // Acknowledge the message
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            
        } catch (Exception e) {
            logger.error("Error processing data logged event for user: {}",
                event.getUserId(), e);
            // In production, you might want to send to a dead letter queue
        }
    }
}
