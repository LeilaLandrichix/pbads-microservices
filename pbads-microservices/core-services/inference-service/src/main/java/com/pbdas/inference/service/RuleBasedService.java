package com.pbdas.inference.service;

import com.pbdas.inference.model.dto.AnomalyResult;
import com.pbdas.inference.model.enums.AnomalyLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RuleBasedService {
    
    private static final Logger logger = LoggerFactory.getLogger(RuleBasedService.class);
    
    // Rule-based thresholds (lowered to catch more anomalies)
    private static final double MIN_SLEEP_HOURS = 6.0;
    private static final double MAX_SLEEP_HOURS = 10.0;
    private static final int MIN_STEPS = 3000; // Lowered from 5000
    private static final int MIN_WATER_ML = 1000; // Lowered from 1500
    private static final int MIN_MOOD_SCORE = 4; // Raised from 3 to catch more
    private static final double MAX_STUDY_HOURS = 12.0;
    
    public AnomalyResult detectAnomaly(String userId, Map<String, Object> features) {
        double anomalyScore = 0.0;
        Map<String, Object> factors = new HashMap<>();
        int anomalyCount = 0;
        
        // Check sleep hours
        if (features.containsKey("sleepHours")) {
            Double sleepHours = getDoubleValue(features.get("sleepHours"));
            if (sleepHours != null) {
                if (sleepHours < MIN_SLEEP_HOURS || sleepHours > MAX_SLEEP_HOURS) {
                    anomalyScore += 0.3;
                    anomalyCount++;
                    factors.put("sleepHours", String.format("Sleep hours: %.1f (normal: %.1f-%.1f)", 
                        sleepHours, MIN_SLEEP_HOURS, MAX_SLEEP_HOURS));
                }
            }
        }
        
        // Check steps
        if (features.containsKey("steps")) {
            Integer steps = getIntegerValue(features.get("steps"));
            if (steps != null && steps < MIN_STEPS) {
                anomalyScore += 0.2;
                anomalyCount++;
                factors.put("steps", String.format("Low activity: %d steps (recommended: %d+)", 
                    steps, MIN_STEPS));
            }
        }
        
        // Check water intake
        if (features.containsKey("waterIntakeMl")) {
            Integer waterIntake = getIntegerValue(features.get("waterIntakeMl"));
            if (waterIntake != null && waterIntake < MIN_WATER_ML) {
                anomalyScore += 0.15;
                anomalyCount++;
                factors.put("waterIntakeMl", String.format("Low water intake: %d ml (recommended: %d+)", 
                    waterIntake, MIN_WATER_ML));
            }
        }
        
        // Check mood score
        if (features.containsKey("moodScore")) {
            Integer moodScore = getIntegerValue(features.get("moodScore"));
            if (moodScore != null && moodScore < MIN_MOOD_SCORE) {
                anomalyScore += 0.25;
                anomalyCount++;
                factors.put("moodScore", String.format("Low mood: %d/10 (normal: %d+)", 
                    moodScore, MIN_MOOD_SCORE));
            }
        }
        
        // Check study hours (excessive)
        if (features.containsKey("studyHours")) {
            Double studyHours = getDoubleValue(features.get("studyHours"));
            if (studyHours != null && studyHours > MAX_STUDY_HOURS) {
                anomalyScore += 0.1;
                anomalyCount++;
                factors.put("studyHours", String.format("Excessive study: %.1f hours (recommended: <%.1f)", 
                    studyHours, MAX_STUDY_HOURS));
            }
        }
        
        // Normalize score to 0-1 range
        anomalyScore = Math.min(1.0, anomalyScore);
        
        // ALWAYS trigger if ANY anomaly factor is detected (very sensitive)
        // This ensures we catch all concerning patterns
        boolean isAnomaly = anomalyCount >= 1 || anomalyScore >= 0.2;
        AnomalyLevel level = determineLevel(anomalyScore);
        
        AnomalyResult result = new AnomalyResult(isAnomaly, anomalyScore, level);
        result.setContributingFactors(factors);
        
        logger.warn("🔍 Rule-based detection for user {}: score={}, count={}, isAnomaly={}, factors={}", 
            userId, anomalyScore, anomalyCount, isAnomaly, factors.keySet());
        
        if (isAnomaly) {
            String factorList = String.join(", ", factors.keySet());
            result.setMessage(String.format("⚠️ Anomaly detected! %d concerning factors: %s (score: %.2f)", 
                anomalyCount, factorList, anomalyScore));
            logger.warn("🚨 ALERT TRIGGERED for user {}: {}", userId, result.getMessage());
        } else {
            result.setMessage("No anomalies detected by rule-based system");
            logger.info("✓ No anomaly for user {}: score={}, count={}", userId, anomalyScore, anomalyCount);
        }
        
        return result;
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
    
    private Integer getIntegerValue(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private AnomalyLevel determineLevel(double score) {
        if (score >= 0.75) return AnomalyLevel.HIGH;
        if (score >= 0.5) return AnomalyLevel.MEDIUM;
        if (score >= 0.25) return AnomalyLevel.LOW;
        return AnomalyLevel.NORMAL;
    }
}
