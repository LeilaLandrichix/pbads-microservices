package com.pbdas.alert.controller;

import com.pbdas.alert.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts/test")
@CrossOrigin(origins = "*")
public class AlertTestController {
    
    @Autowired
    private AlertService alertService;
    
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createTestAlert(
            @RequestParam(defaultValue = "admin") String userId,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "0.8") Double anomalyScore,
            @RequestParam(defaultValue = "HIGH") String anomalyLevel,
            @RequestParam(required = false) String message) {
        
        try {
            String alertDate = date != null ? date : LocalDate.now().toString();
            String alertMessage = message != null ? message : "Test alert: Anomaly detected - Low sleep and mood";
            
            Map<String, Object> factors = new HashMap<>();
            factors.put("sleepHours", "Sleep hours: 4.0 (normal: 6.0-10.0)");
            factors.put("moodScore", "Low mood: 2/10 (normal: 4+)");
            factors.put("steps", "Low activity: 2000 steps (recommended: 3000+)");
            
            var alert = alertService.createAlert(
                userId,
                alertDate,
                anomalyScore,
                anomalyLevel,
                alertMessage,
                factors
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "alertId", alert.getId(),
                "message", "Test alert created successfully",
                "alert", Map.of(
                    "id", alert.getId(),
                    "userId", alert.getUserId(),
                    "date", alert.getDate(),
                    "anomalyScore", alert.getAnomalyScore(),
                    "anomalyLevel", alert.getAnomalyLevel(),
                    "message", alert.getMessage()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAlerts(@RequestParam(defaultValue = "admin") String userId) {
        var alerts = alertService.getAlertsByUserId(userId);
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "alertCount", alerts.size(),
            "alerts", alerts.stream().map(a -> Map.of(
                "id", a.getId(),
                "date", a.getDate(),
                "score", a.getAnomalyScore(),
                "level", a.getAnomalyLevel(),
                "message", a.getMessage()
            )).toList()
        ));
    }
}
