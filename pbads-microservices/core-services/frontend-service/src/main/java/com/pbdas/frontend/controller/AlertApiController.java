package com.pbdas.frontend.controller;

import com.pbdas.frontend.service.ApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AlertApiController {
    
    @Autowired
    private ApiService apiService;
    
    @GetMapping("/alerts")
    public ResponseEntity<List<Map<String, Object>>> getAlerts(
            @RequestParam(required = false) String userId,
            HttpSession session) {
        
        String token = (String) session.getAttribute("token");
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        
        String user = userId != null ? userId : (String) session.getAttribute("username");
        if (user == null) {
            user = "admin";
        }
        
        List<Map<String, Object>> alerts = apiService.getAlerts(token, user);
        return ResponseEntity.ok(alerts);
    }
}

