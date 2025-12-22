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
public class AlertsController {
    
    @Autowired
    private ApiService apiService;
    
    @GetMapping("/alerts")
    public String alertsPage(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            return "redirect:/login";
        }
        
        String userId = (String) session.getAttribute("username");
        List<Map<String, Object>> alerts = apiService.getAlerts(token, userId);
        String username = (String) session.getAttribute("username");
        model.addAttribute("alerts", alerts);
        model.addAttribute("hasAlerts", alerts != null && !alerts.isEmpty());
        model.addAttribute("username", username);
        return "alerts";
    }
}

