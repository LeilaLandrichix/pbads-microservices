package com.pbdas.inference.service;

import com.pbdas.inference.model.dto.AnomalyResult;
import com.pbdas.inference.model.enums.AnomalyLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AnomalyDetectorService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnomalyDetectorService.class);
    
    @Autowired
    private ModelLoaderService modelLoaderService;
    
    @Autowired
    private RuleBasedService ruleBasedService;
    
    @Value("${inference.thresholds.anomaly-score.low:0.5}")
    private double lowThreshold;
    
    @Value("${inference.thresholds.anomaly-score.medium:0.75}")
    private double mediumThreshold;
    
    @Value("${inference.thresholds.anomaly-score.high:0.9}")
    private double highThreshold;
    
    @Value("${inference.rule-based.enabled:true}")
    private boolean ruleBasedEnabled;
    
    public AnomalyResult detectAnomaly(String userId, Map<String, Object> features, double threshold) {
        try {
            // Try ML model first
            Double mlScore = modelLoaderService.predictAnomalyScore(userId, features);
            
            if (mlScore != null) {
                return createAnomalyResult(mlScore, threshold, features);
            }
            
            // Fallback to rule-based detection
            if (ruleBasedEnabled) {
                return ruleBasedService.detectAnomaly(userId, features);
            }
            
            // Default: no anomaly
            return new AnomalyResult(false, 0.0, AnomalyLevel.NORMAL);
            
        } catch (Exception e) {
            logger.error("Error in anomaly detection for user: {}", userId, e);
            // Fallback to rule-based
            if (ruleBasedEnabled) {
                return ruleBasedService.detectAnomaly(userId, features);
            }
            return new AnomalyResult(false, 0.0, AnomalyLevel.NORMAL);
        }
    }
    
    private AnomalyResult createAnomalyResult(double score, double threshold, Map<String, Object> features) {
        boolean isAnomaly = score >= threshold;
        AnomalyLevel level = determineLevel(score);
        
        AnomalyResult result = new AnomalyResult(isAnomaly, score, level);
        
        // Create message
        if (isAnomaly) {
            result.setMessage(String.format("Anomaly detected with score: %.2f (threshold: %.2f)", 
                score, threshold));
        } else {
            result.setMessage("No anomaly detected");
        }
        
        // Identify contributing factors
        Map<String, Object> factors = new HashMap<>();
        if (features.containsKey("sleepHours")) {
            Double sleepHours = (Double) features.get("sleepHours");
            if (sleepHours != null && (sleepHours < 6 || sleepHours > 10)) {
                factors.put("sleepHours", "Unusual sleep duration: " + sleepHours);
            }
        }
        if (features.containsKey("moodScore")) {
            Integer moodScore = (Integer) features.get("moodScore");
            if (moodScore != null && moodScore < 3) {
                factors.put("moodScore", "Low mood score: " + moodScore);
            }
        }
        if (features.containsKey("steps")) {
            Integer steps = (Integer) features.get("steps");
            if (steps != null && steps < 5000) {
                factors.put("steps", "Low activity: " + steps + " steps");
            }
        }
        
        result.setContributingFactors(factors);
        return result;
    }
    
    private AnomalyLevel determineLevel(double score) {
        if (score >= highThreshold) {
            return AnomalyLevel.HIGH;
        } else if (score >= mediumThreshold) {
            return AnomalyLevel.MEDIUM;
        } else if (score >= lowThreshold) {
            return AnomalyLevel.LOW;
        } else {
            return AnomalyLevel.NORMAL;
        }
    }
}
