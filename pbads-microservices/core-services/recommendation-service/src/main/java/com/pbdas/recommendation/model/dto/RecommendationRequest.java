package com.pbdas.recommendation.model.dto;

import java.util.Map;

public class RecommendationRequest {
    private String userId;
    private Map<String, Object> userData;
    private String dataSummary;
    
    public RecommendationRequest() {
    }
    
    public RecommendationRequest(String userId, Map<String, Object> userData, String dataSummary) {
        this.userId = userId;
        this.userData = userData;
        this.dataSummary = dataSummary;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Map<String, Object> getUserData() {
        return userData;
    }
    
    public void setUserData(Map<String, Object> userData) {
        this.userData = userData;
    }
    
    public String getDataSummary() {
        return dataSummary;
    }
    
    public void setDataSummary(String dataSummary) {
        this.dataSummary = dataSummary;
    }
}

