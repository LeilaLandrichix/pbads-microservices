package com.pbdas.data.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class DailyDataDTO {
    
    private Long id;
    
    @NotBlank(message = "User ID is required")
    @Size(max = 50, message = "User ID must not exceed 50 characters")
    private String userId;
    
    @NotNull(message = "Date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime wakeUpTime; // Accepts HH:mm format from HTML time input
    
    @Min(value = 0, message = "Sleep hours must be positive")
    @Max(value = 24, message = "Sleep hours cannot exceed 24")
    private Double sleepHours;
    
    @Min(value = 0, message = "Steps must be positive")
    private Integer steps;
    
    @Min(value = 0, message = "Calories burned must be positive")
    private Integer caloriesBurned;
    
    @Min(value = 0, message = "Water intake must be positive")
    private Integer waterIntakeMl;
    
    @Min(value = 0, message = "Study hours must be positive")
    @Max(value = 24, message = "Study hours cannot exceed 24")
    private Double studyHours;
    
    @Min(value = 1, message = "Mood score must be between 1 and 10")
    @Max(value = 10, message = "Mood score must be between 1 and 10")
    private Integer moodScore;
    
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
}
