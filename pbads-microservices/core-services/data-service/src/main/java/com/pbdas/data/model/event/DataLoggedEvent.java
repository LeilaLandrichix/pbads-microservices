package com.pbdas.data.model.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public class DataLoggedEvent {
    
    private String userId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime wakeUpTime;
    
    private Double sleepHours;
    private Integer steps;
    private Integer caloriesBurned;
    private Integer waterIntakeMl;
    private Double studyHours;
    private Integer moodScore;
    
    // Getters and Setters
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
