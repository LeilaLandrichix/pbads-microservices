package com.pbdas.data.kafka.producer;

import com.pbdas.data.model.event.DataLoggedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class DataEventProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(DataEventProducer.class);
    
    @Autowired
    private KafkaTemplate<String, DataLoggedEvent> kafkaTemplate;
    
    @Value("${kafka.topics.data-logged:data.logged.events}")
    private String dataLoggedTopic;
    
    public void sendDataLoggedEvent(DataLoggedEvent event) {
        try {
            CompletableFuture<SendResult<String, DataLoggedEvent>> future = 
                kafkaTemplate.send(dataLoggedTopic, event.getUserId(), event);
            
            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    logger.info("Sent data logged event for user: {} on date: {}",
                        event.getUserId(), event.getDate());
                } else {
                    logger.error("Failed to send data logged event for user: {}",
                        event.getUserId(), exception);
                }
            });
        } catch (Exception e) {
            logger.error("Error sending data logged event", e);
        }
    }
}
