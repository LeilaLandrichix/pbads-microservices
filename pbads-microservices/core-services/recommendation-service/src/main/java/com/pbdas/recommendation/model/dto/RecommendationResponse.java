package com.pbdas.recommendation.model.dto;

public class RecommendationResponse {
    private String userId;
    private String recommendation;
    private String timestamp;
    
    public RecommendationResponse() {
    }
    
    public RecommendationResponse(String userId, String recommendation, String timestamp) {
        this.userId = userId;
        this.recommendation = recommendation;
        this.timestamp = timestamp;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

