package com.pbdas.inference.controller;

import com.pbdas.data.model.event.DataLoggedEvent;
import com.pbdas.inference.model.dto.AnomalyResult;
import com.pbdas.inference.service.InferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/inference")
@CrossOrigin(origins = "*")
public class InferenceController {
    
    @Autowired
    private InferenceService inferenceService;
    
    @PostMapping("/detect")
    public ResponseEntity<AnomalyResult> detectAnomaly(
            @RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            String dateStr = (String) request.get("date");
            LocalDate date = dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now();
            
            Map<String, Object> features = new HashMap<>();
            if (request.containsKey("sleepHours")) features.put("sleepHours", request.get("sleepHours"));
            if (request.containsKey("steps")) features.put("steps", request.get("steps"));
            if (request.containsKey("caloriesBurned")) features.put("caloriesBurned", request.get("caloriesBurned"));
            if (request.containsKey("waterIntakeMl")) features.put("waterIntakeMl", request.get("waterIntakeMl"));
            if (request.containsKey("studyHours")) features.put("studyHours", request.get("studyHours"));
            if (request.containsKey("moodScore")) features.put("moodScore", request.get("moodScore"));
            
            AnomalyResult result = inferenceService.detectAnomaly(userId != null ? userId : "admin", features);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @PostMapping("/process-event")
    public ResponseEntity<Map<String, Object>> processDataEvent(@RequestBody DataLoggedEvent event) {
        try {
            inferenceService.processDataForAnomalyDetection(event);
            return ResponseEntity.ok(Map.of("status", "processed", "userId", event.getUserId()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}

