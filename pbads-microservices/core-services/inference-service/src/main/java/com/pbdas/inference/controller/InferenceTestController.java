package com.pbdas.inference.controller;

import com.pbdas.inference.model.dto.AnomalyResult;
import com.pbdas.inference.service.InferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/inference/test")
@CrossOrigin(origins = "*")
public class InferenceTestController {
    
    @Autowired
    private InferenceService inferenceService;
    
    @PostMapping("/detect-sample")
    public ResponseEntity<Map<String, Object>> testDetection() {
        // Test with your exact problematic data
        Map<String, Object> features = new HashMap<>();
        features.put("sleepHours", 4.0);  // Low sleep
        features.put("moodScore", 2);    // Very low mood
        features.put("steps", 2000);     // Low steps
        features.put("waterIntakeMl", 500); // Low water
        
        AnomalyResult result = inferenceService.detectAnomaly("admin", features);
        
        Map<String, Object> response = new HashMap<>();
        response.put("isAnomaly", result.isAnomaly());
        response.put("anomalyScore", result.getAnomalyScore());
        response.put("level", result.getLevel());
        response.put("message", result.getMessage());
        response.put("contributingFactors", result.getContributingFactors());
        response.put("testData", features);
        
        return ResponseEntity.ok(response);
    }
}

