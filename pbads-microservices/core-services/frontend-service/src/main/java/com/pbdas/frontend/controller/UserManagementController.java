package com.pbdas.frontend.controller;

import com.pbdas.frontend.service.ApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class UserManagementController {
    
    @Autowired
    private ApiService apiService;
    
    @GetMapping("/users")
    public String usersPage(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");
        String username = (String) session.getAttribute("username");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        
        if (token == null) {
            return "redirect:/login";
        }
        
        // Check if user is admin
        if (!Boolean.TRUE.equals(isAdmin) && !"admin".equals(username)) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        try {
            List<Map<String, Object>> users = apiService.getAllUsers(token);
            model.addAttribute("users", users != null ? users : List.of());
            model.addAttribute("hasUsers", users != null && !users.isEmpty());
            model.addAttribute("activePage", "users");
            return "users";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to load users: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }
    
    @PostMapping("/users/{id}/toggle")
    public String toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        String token = (String) session.getAttribute("token");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        
        if (token == null || !Boolean.TRUE.equals(isAdmin)) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        try {
            Map<String, Object> userData = Map.of("enabled", enabled);
            boolean success = apiService.updateUser(id, userData, token);
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", "User status updated successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to update user status");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to update user status: " + e.getMessage());
        }
        
        return "redirect:/users";
    }
    
    @PostMapping("/users/{id}/delete")
    public String deleteUser(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        String token = (String) session.getAttribute("token");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        
        if (token == null || !Boolean.TRUE.equals(isAdmin)) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        try {
            boolean success = apiService.deleteUser(id, token);
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", "User deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to delete user");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete user: " + e.getMessage());
        }
        
        return "redirect:/users";
    }
}

