package com.pbdas.data.model.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_data", indexes = {
    @Index(name = "idx_user_date", columnList = "user_id,date"),
    @Index(name = "idx_date", columnList = "date")
})
public class DailyData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "wake_up_time")
    private LocalTime wakeUpTime;
    
    @Column(name = "sleep_hours")
    private Double sleepHours;
    
    @Column(name = "steps")
    private Integer steps;
    
    @Column(name = "calories_burned")
    private Integer caloriesBurned;
    
    @Column(name = "water_intake_ml")
    private Integer waterIntakeMl;
    
    @Column(name = "study_hours")
    private Double studyHours;
    
    @Column(name = "mood_score")
    private Integer moodScore; // 1-10 scale
    
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
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public LocalTime getWakeUpTime() {
        return wakeUpTime;
    }
    
    public void setWakeUpTime(LocalTime wakeUpTime) {
        this.wakeUpTime = wakeUpTime;
    }
    
    public Double getSleepHours() {
        return sleepHours;
    }
    
    public void setSleepHours(Double sleepHours) {
        this.sleepHours = sleepHours;
    }
    
    public Integer getSteps() {
        return steps;
    }
    
    public void setSteps(Integer steps) {
        this.steps = steps;
    }
    
    public Integer getCaloriesBurned() {
        return caloriesBurned;
    }
    
    public void setCaloriesBurned(Integer caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }
    
    public Integer getWaterIntakeMl() {
        return waterIntakeMl;
    }
    
    public void setWaterIntakeMl(Integer waterIntakeMl) {
        this.waterIntakeMl = waterIntakeMl;
    }
    
    public Double getStudyHours() {
        return studyHours;
    }
    
    public void setStudyHours(Double studyHours) {
        this.studyHours = studyHours;
    }
    
    public Integer getMoodScore() {
        return moodScore;
    }
    
    public void setMoodScore(Integer moodScore) {
        this.moodScore = moodScore;
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
