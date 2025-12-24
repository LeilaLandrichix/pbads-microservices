package com.pbdas.recommendation.controller;

import com.pbdas.recommendation.model.dto.RecommendationRequest;
import com.pbdas.recommendation.model.dto.RecommendationResponse;
import com.pbdas.recommendation.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationController {
    
    @Autowired
    private GeminiService geminiService;
    
    @PostMapping("/generate")
    public ResponseEntity<RecommendationResponse> generateRecommendation(
            @RequestBody RecommendationRequest request) {
        
        String recommendation = geminiService.generateRecommendation(request.getDataSummary());
        
        RecommendationResponse response = new RecommendationResponse(
            request.getUserId(),
            recommendation,
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<RecommendationResponse> getRecommendationForUser(
            @PathVariable String userId,
            @RequestParam(required = false) String dataSummary) {
        
        // If no data summary provided, use a default message
        String summary = dataSummary != null ? dataSummary : 
            "User data not available. Please provide data summary for personalized recommendations.";
        
        String recommendation = geminiService.generateRecommendation(summary);
        
        RecommendationResponse response = new RecommendationResponse(
            userId,
            recommendation,
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        
        return ResponseEntity.ok(response);
    }
}

