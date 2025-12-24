package com.pbdas.alert.controller;

import com.pbdas.alert.model.entity.Alert;
import com.pbdas.alert.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {
    
    @Autowired
    private AlertService alertService;
    
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllAlerts(
            @RequestParam(required = false) String userId) {
        List<Alert> alerts;
        
        if (userId != null && !userId.isEmpty()) {
            alerts = alertService.getAlertsByUserId(userId);
        } else {
            alerts = alertService.getAllActiveAlerts();
        }
        
        List<Map<String, Object>> alertList = alerts.stream()
            .map(this::convertToMap)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(alertList);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveAlerts(
            @RequestParam(required = false) String userId) {
        List<Alert> alerts;
        
        if (userId != null && !userId.isEmpty()) {
            alerts = alertService.getActiveAlertsByUserId(userId);
        } else {
            alerts = alertService.getAllActiveAlerts();
        }
        
        List<Map<String, Object>> alertList = alerts.stream()
            .map(this::convertToMap)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(alertList);
    }
    
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<Map<String, Object>> acknowledgeAlert(@PathVariable Long id) {
        try {
            Alert alert = alertService.acknowledgeAlert(id);
            return ResponseEntity.ok(convertToMap(alert));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/resolve")
    public ResponseEntity<Map<String, Object>> resolveAlert(@PathVariable Long id) {
        try {
            Alert alert = alertService.resolveAlert(id);
            return ResponseEntity.ok(convertToMap(alert));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/create-direct")
    public ResponseEntity<Map<String, Object>> createAlertDirectly(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            String date = (String) request.get("date");
            Double anomalyScore = getDoubleValue(request.get("anomalyScore"));
            String anomalyLevel = (String) request.get("anomalyLevel");
            String message = (String) request.get("message");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> contributingFactors = (Map<String, Object>) request.get("contributingFactors");
            String recommendation = (String) request.get("recommendation");
            
            if (userId == null || date == null || anomalyScore == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }
            
            Alert alert = alertService.createAlert(
                userId,
                date,
                anomalyScore,
                anomalyLevel != null ? anomalyLevel : "MEDIUM",
                message != null ? message : "Anomaly detected",
                contributingFactors,
                recommendation
            );
            
            return ResponseEntity.ok(convertToMap(alert));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    private Double getDoubleValue(Object value) {
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Alert Service is running");
    }
    
    private Map<String, Object> convertToMap(Alert alert) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", alert.getId());
        map.put("userId", alert.getUserId());
        map.put("date", alert.getDate());
        map.put("anomalyScore", alert.getAnomalyScore());
        map.put("anomalyLevel", alert.getAnomalyLevel());
        map.put("message", alert.getMessage());
        map.put("status", alert.getStatus());
        map.put("recommendation", alert.getRecommendation());
        map.put("createdAt", alert.getCreatedAt() != null ? alert.getCreatedAt().toString() : null);
        map.put("timestamp", alert.getCreatedAt() != null ? alert.getCreatedAt().toString() : null);
        
        // Parse contributing factors if present
        if (alert.getContributingFactors() != null && !alert.getContributingFactors().isEmpty()) {
            try {
                // Try to parse as JSON, otherwise use as string
                map.put("contributingFactors", alert.getContributingFactors());
            } catch (Exception e) {
                map.put("contributingFactors", alert.getContributingFactors());
            }
        }
        
        return map;
    }
}
