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

import java.util.Map;

@Controller
public class LoginController {
    
    @Autowired
    private ApiService apiService;
    
    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        // If already logged in, redirect to dashboard
        if (session.getAttribute("token") != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("activePage", "login");
        return "login";
    }
    
    @PostMapping("/login")
    public String login(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // Require username and password - no auto-login
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Username and password are required.");
            return "redirect:/login";
        }
        
        String token = apiService.login(username, password);
        
        if (token != null && !token.isEmpty()) {
            session.setAttribute("token", token);
            session.setAttribute("username", username);
            
            // Fetch user details to get firstname
            Map<String, Object> userInfo = apiService.getCurrentUser(token);
            if (userInfo != null) {
                String firstName = (String) userInfo.get("firstName");
                String role = (String) userInfo.get("role");
                if (firstName != null && !firstName.isEmpty()) {
                    session.setAttribute("firstName", firstName);
                } else {
                    // Fallback to username if no firstname
                    session.setAttribute("firstName", username);
                }
                // Check if admin based on role
                session.setAttribute("isAdmin", "ADMIN".equals(role));
            } else {
                // Fallback if user info can't be fetched
                session.setAttribute("firstName", username);
                session.setAttribute("isAdmin", "admin".equals(username));
            }
            
            return "redirect:/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid username or password. Please check your credentials and ensure the Auth Service is running.");
            return "redirect:/login";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
