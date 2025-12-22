package com.pbdas.auth.config;

import com.pbdas.auth.model.entity.User;
import com.pbdas.auth.repository.UserRepository;
import com.pbdas.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // First, update all existing users that have null role to "USER"
        var allUsers = userRepository.findAll();
        boolean updatedAny = false;
        for (var user : allUsers) {
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("USER");
                userRepository.save(user);
                updatedAny = true;
            }
        }
        if (updatedAny) {
            System.out.println("Updated existing users with null roles to USER");
        }
        
        // Create default admin user
        // Username: admin, Password: admin123
        if (!userRepository.existsByUsername("admin")) {
            authService.createDefaultUser(
                "admin",
                "admin123",
                "admin@pbads.com"
            );
            System.out.println("========================================");
            System.out.println("Default user created:");
            System.out.println("Username: admin");
            System.out.println("Password: admin123");
            System.out.println("Role: ADMIN");
            System.out.println("========================================");
        } else {
            // Update existing admin user to ensure it has ADMIN role
            var existingAdmin = userRepository.findByUsername("admin");
            if (existingAdmin.isPresent()) {
                var admin = existingAdmin.get();
                if (!"ADMIN".equals(admin.getRole())) {
                    admin.setRole("ADMIN");
                    admin.setEnabled(true);
                    userRepository.save(admin);
                    System.out.println("Updated existing admin user to ADMIN role");
                } else {
                    System.out.println("Default user 'admin' already exists with ADMIN role");
                }
            }
        }
    }
}

