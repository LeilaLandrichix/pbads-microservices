package com.pbdas.inference.model.dto;

import com.pbdas.inference.model.enums.AnomalyLevel;
import java.util.Map;

public class AnomalyResult {
    
    private boolean anomaly;
    private double anomalyScore;
    private AnomalyLevel level;
    private String message;
    private Map<String, Object> contributingFactors;
    
    public AnomalyResult() {
    }
    
    public AnomalyResult(boolean anomaly, double anomalyScore, AnomalyLevel level) {
        this.anomaly = anomaly;
        this.anomalyScore = anomalyScore;
        this.level = level;
    }
    
    // Getters and Setters
    public boolean isAnomaly() {
        return anomaly;
    }
    
    public void setAnomaly(boolean anomaly) {
        this.anomaly = anomaly;
    }
    
    public double getAnomalyScore() {
        return anomalyScore;
    }
    
    public void setAnomalyScore(double anomalyScore) {
        this.anomalyScore = anomalyScore;
    }
    
    public AnomalyLevel getLevel() {
        return level;
    }
    
    public void setLevel(AnomalyLevel level) {
        this.level = level;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Map<String, Object> getContributingFactors() {
        return contributingFactors;
    }
    
    public void setContributingFactors(Map<String, Object> contributingFactors) {
        this.contributingFactors = contributingFactors;
    }
}
