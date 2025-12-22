package com.pbdas.frontend.controller;

import com.pbdas.frontend.service.ApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
public class DataEntryController {
    
    @Autowired
    private ApiService apiService;
    
    @GetMapping("/data")
    public String dataEntryPage(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            return "redirect:/login";
        }
        
        String username = (String) session.getAttribute("username");
        model.addAttribute("today", LocalDate.now().toString());
        model.addAttribute("username", username);
        return "data-entry";
    }
    
    @PostMapping("/data")
    public String saveData(
            @RequestParam String date,
            @RequestParam(required = false) String wakeUpTime,
            @RequestParam(required = false) String sleepHours,
            @RequestParam(required = false) String steps,
            @RequestParam(required = false) String caloriesBurned,
            @RequestParam(required = false) String waterIntakeMl,
            @RequestParam(required = false) String studyHours,
            @RequestParam(required = false) String moodScore,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        String token = (String) session.getAttribute("token");
        if (token == null) {
            return "redirect:/login";
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", session.getAttribute("username") != null ? session.getAttribute("username") : "1");
        data.put("date", date); // Date is already in ISO format (YYYY-MM-DD) from HTML date input
        
        // Only add fields that have values
        if (wakeUpTime != null && !wakeUpTime.isEmpty()) {
            // HTML time input gives HH:MM format (e.g., "09:30")
            // Spring Boot should handle this automatically, but we'll keep it as-is
            // The @JsonFormat(pattern = "HH:mm") in DTO will handle the parsing
            data.put("wakeUpTime", wakeUpTime);
        }
        if (sleepHours != null && !sleepHours.isEmpty()) {
            try {
                data.put("sleepHours", Double.parseDouble(sleepHours));
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("error", "Invalid sleep hours format");
                return "redirect:/data";
            }
        }
        if (steps != null && !steps.isEmpty()) {
            try {
                data.put("steps", Integer.parseInt(steps));
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("error", "Invalid steps format");
                return "redirect:/data";
            }
        }
        if (caloriesBurned != null && !caloriesBurned.isEmpty()) {
            try {
                data.put("caloriesBurned", Integer.parseInt(caloriesBurned));
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("error", "Invalid calories burned format");
                return "redirect:/data";
            }
        }
        if (waterIntakeMl != null && !waterIntakeMl.isEmpty()) {
            try {
                data.put("waterIntakeMl", Integer.parseInt(waterIntakeMl));
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("error", "Invalid water intake format");
                return "redirect:/data";
            }
        }
        if (studyHours != null && !studyHours.isEmpty()) {
            try {
                data.put("studyHours", Double.parseDouble(studyHours));
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("error", "Invalid study hours format");
                return "redirect:/data";
            }
        }
        if (moodScore != null && !moodScore.isEmpty()) {
            try {
                data.put("moodScore", Integer.parseInt(moodScore));
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("error", "Invalid mood score format");
                return "redirect:/data";
            }
        }
        
        boolean success = apiService.saveDailyData(data, token);
        
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Data saved successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to save data. Please try again.");
        }
        
        return "redirect:/data";
    }
}

