package com.pbdas.alert.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts", indexes = {
    @Index(name = "idx_user_date", columnList = "userId,createdAt"),
    @Index(name = "idx_user_status", columnList = "userId,status")
})
public class Alert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;
    
    @Column(name = "date", nullable = false)
    private String date; // Store as string for simplicity
    
    @Column(name = "anomaly_score", nullable = false)
    private Double anomalyScore;
    
    @Column(name = "anomaly_level", nullable = false, length = 20)
    private String anomalyLevel; // LOW, MEDIUM, HIGH, NORMAL
    
    @Column(name = "message", length = 500)
    private String message;
    
    @Column(name = "contributing_factors", columnDefinition = "TEXT")
    private String contributingFactors; // JSON string
    
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE"; // ACTIVE, ACKNOWLEDGED, RESOLVED
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public Double getAnomalyScore() {
        return anomalyScore;
    }
    
    public void setAnomalyScore(Double anomalyScore) {
        this.anomalyScore = anomalyScore;
    }
    
    public String getAnomalyLevel() {
        return anomalyLevel;
    }
    
    public void setAnomalyLevel(String anomalyLevel) {
        this.anomalyLevel = anomalyLevel;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getContributingFactors() {
        return contributingFactors;
    }
    
    public void setContributingFactors(String contributingFactors) {
        this.contributingFactors = contributingFactors;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
