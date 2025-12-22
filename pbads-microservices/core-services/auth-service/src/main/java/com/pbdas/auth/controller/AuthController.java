package com.pbdas.auth.controller;

import com.pbdas.auth.model.dto.AuthRequest;
import com.pbdas.auth.model.dto.AuthResponse;
import com.pbdas.auth.model.dto.RegisterRequest;
import com.pbdas.auth.model.dto.UserDTO;
import com.pbdas.auth.service.AuthService;
import com.pbdas.auth.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtService jwtService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            var user = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "User registered successfully",
                "username", user.getUsername(),
                "id", user.getId()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = extractUsernameFromHeader(authHeader);
        if (username == null || !authService.isAdmin(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(authService.getAllUsers());
    }
    
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = extractUsernameFromHeader(authHeader);
        if (username == null || !authService.isAdmin(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return authService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/users/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = extractUsernameFromHeader(authHeader);
        if (username == null || !authService.isAdmin(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            return ResponseEntity.ok(authService.updateUser(id, userDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = extractUsernameFromHeader(authHeader);
        if (username == null || !authService.isAdmin(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            authService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = extractUsernameFromHeader(authHeader);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        return authService.getAllUsers().stream()
            .filter(u -> u.getUsername().equals(username))
            .findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    private String extractUsernameFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            return jwtService.extractUsername(token);
        } catch (Exception e) {
            return null;
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running");
    }
    
    @GetMapping("/test-admin")
    public ResponseEntity<Map<String, Object>> testAdmin() {
        var adminOpt = authService.getAllUsers().stream()
            .filter(u -> "admin".equals(u.getUsername()))
            .findFirst();
        
        if (adminOpt.isPresent()) {
            var admin = adminOpt.get();
            return ResponseEntity.ok(Map.of(
                "exists", true,
                "username", admin.getUsername(),
                "role", admin.getRole() != null ? admin.getRole() : "NULL",
                "enabled", admin.isEnabled()
            ));
        } else {
            return ResponseEntity.ok(Map.of("exists", false));
        }
    }
}
