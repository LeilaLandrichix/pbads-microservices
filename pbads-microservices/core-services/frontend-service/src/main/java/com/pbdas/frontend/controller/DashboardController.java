package com.pbdas.frontend.controller;

import com.pbdas.frontend.service.ApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {
    
    @Autowired
    private ApiService apiService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        String username = (String) session.getAttribute("username");
        
        if (token == null) {
            return "redirect:/login";
        }
        
        // Get dashboard data for user (use username, default to "admin")
        String userId = username != null ? username : "admin";
        List<Map<String, Object>> data = apiService.getDashboardData(userId, token);
        
        // Get recent alerts
        List<Map<String, Object>> alerts = apiService.getAlerts(token, userId);
        
        // Get recent activity (last 5 data entries)
        List<Map<String, Object>> recentActivity = data != null && data.size() > 5 
            ? data.subList(0, Math.min(5, data.size())) 
            : (data != null ? data : List.of());
        
        // Generate data summary for recommendations
        String dataSummary = generateDataSummary(data);
        
        // Get recommendation
        String recommendation = apiService.getRecommendation(userId, dataSummary, token);
        
        model.addAttribute("data", data);
        model.addAttribute("username", username);
        model.addAttribute("hasData", data != null && !data.isEmpty());
        model.addAttribute("alerts", alerts);
        model.addAttribute("alertCount", alerts != null ? alerts.size() : 0);
        model.addAttribute("recentActivity", recentActivity);
        model.addAttribute("recommendation", recommendation);
        model.addAttribute("activePage", "dashboard");
        
        return "dashboard";
    }
    
    private String generateDataSummary(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return "No data available for analysis.";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Recent habit data:\n");
        
        int count = Math.min(7, data.size());
        for (int i = 0; i < count; i++) {
            Map<String, Object> entry = data.get(i);
            summary.append(String.format("Date: %s - ", entry.get("date")));
            if (entry.get("sleepHours") != null) {
                summary.append(String.format("Sleep: %.1fh, ", entry.get("sleepHours")));
            }
            if (entry.get("steps") != null) {
                summary.append(String.format("Steps: %s, ", entry.get("steps")));
            }
            if (entry.get("waterIntakeMl") != null) {
                summary.append(String.format("Water: %sml, ", entry.get("waterIntakeMl")));
            }
            if (entry.get("moodScore") != null) {
                summary.append(String.format("Mood: %s/10", entry.get("moodScore")));
            }
            summary.append("\n");
        }
        
        return summary.toString();
    }
}

