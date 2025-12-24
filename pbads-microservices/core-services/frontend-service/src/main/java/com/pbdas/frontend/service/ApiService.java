package com.pbdas.frontend.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApiService {
    
    private final RestTemplate restTemplate;
    
    @Value("${api.gateway.url:http://localhost:8080}")
    private String gatewayUrl;
    
    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;
    
    @Value("${data.service.url:http://localhost:8082}")
    private String dataServiceUrl;
    
    @Value("${alert.service.url:http://localhost:8085}")
    private String alertServiceUrl;
    
    @Value("${recommendation.service.url:http://localhost:8087}")
    private String recommendationServiceUrl;
    
    public ApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @CircuitBreaker(name = "apiService", fallbackMethod = "loginFallback")
    @Retry(name = "apiService")
    public String login(String username, String password) {
        // Try gateway first - let exceptions propagate so Resilience4j can retry
        String url = gatewayUrl + "/api/auth/login";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, String> request = Map.of(
            "username", username,
            "password", password
        );
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        
        System.out.println("========================================");
        System.out.println("[Resilience4j] Attempting login to: " + url);
        System.out.println("[Resilience4j] Username: " + username);
        System.out.println("[Resilience4j] (Watch for retry events in logs if this fails)");
        System.out.println("========================================");
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Object tokenObj = body.get("token");
                if (tokenObj != null) {
                    System.out.println("[Resilience4j] Login successful, token received from: " + url);
                    return tokenObj.toString();
                }
            }
            // If no token, throw exception to trigger fallback
            throw new RuntimeException("Login response missing token field");
        } catch (HttpClientErrorException e) {
            // If 401, credentials are wrong - don't retry
            if (e.getStatusCode().value() == 401) {
                throw e; // Let it propagate - don't retry wrong credentials
            }
            // For 404 or other errors, throw to trigger retry/fallback
            System.err.println("[Resilience4j] Login failed with HTTP " + e.getStatusCode() + " - will retry or use fallback");
            throw new RestClientException("Gateway login failed: " + e.getMessage(), e);
        }
        // Any other exception will propagate and trigger Resilience4j retry/fallback
    }
    
    public String loginFallback(String username, String password, Exception e) {
        System.err.println("========================================");
        System.err.println("[Resilience4j] Circuit breaker opened or retry exhausted for login");
        System.err.println("[Resilience4j] Error: " + e.getMessage());
        System.err.println("[Resilience4j] Trying fallback: direct auth service");
        System.err.println("========================================");
        
        // Fallback: try direct auth service
        String url = authServiceUrl + "/api/auth/login";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, String> request = Map.of(
            "username", username,
            "password", password
        );
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        
        try {
            System.out.println("[Resilience4j Fallback] Attempting login to: " + url);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Object tokenObj = body.get("token");
                if (tokenObj != null) {
                    System.out.println("[Resilience4j Fallback] Login successful via direct service");
                    return tokenObj.toString();
                }
            }
        } catch (Exception fallbackException) {
            System.err.println("[Resilience4j Fallback] Direct service also failed: " + fallbackException.getMessage());
        }
        
        return null; // Both failed
    }
    
    @CircuitBreaker(name = "apiService", fallbackMethod = "getCurrentUserFallback")
    @Retry(name = "apiService")
    public Map<String, Object> getCurrentUser(String token) {
        String[] urls = {
            gatewayUrl + "/api/auth/me",
            authServiceUrl + "/api/auth/me"
        };
        
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        for (String url : urls) {
            try {
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
                );
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 404 && url.equals(gatewayUrl + "/api/auth/me")) {
                    continue; // Try direct service
                }
                return null;
            } catch (Exception e) {
                if (url.equals(gatewayUrl + "/api/auth/me")) {
                    continue; // Try direct service
                }
                return null;
            }
        }
        return null;
    }
    
    public Map<String, Object> getCurrentUserFallback(String token, Exception e) {
        System.err.println("Circuit breaker opened or retry exhausted for getCurrentUser. Error: " + e.getMessage());
        return null;
    }
    
    @CircuitBreaker(name = "apiService", fallbackMethod = "getDashboardDataFallback")
    @Retry(name = "apiService")
    public List<Map<String, Object>> getDashboardData(String userId, String token) {
        // Try gateway first, fallback to direct data service if gateway fails
        String[] urls = {
            gatewayUrl + "/api/data/daily/user/" + userId,
            dataServiceUrl + "/api/data/daily/user/" + userId
        };
        
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        for (String url : urls) {
            try {
                ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url, 
                    HttpMethod.GET, 
                    entity, 
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                );
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
                }
            } catch (HttpClientErrorException e) {
                // If 404 from gateway, try direct data service
                if (e.getStatusCode().value() == 404 && url.contains(gatewayUrl)) {
                    continue;
                }
                // Other errors, break
                break;
            } catch (Exception e) {
                // Try next URL if this one fails
                if (url.contains(gatewayUrl)) {
                    continue;
                }
            }
        }
        return Collections.emptyList();
    }
    
    public List<Map<String, Object>> getDashboardDataFallback(String userId, String token, Exception e) {
        System.err.println("Circuit breaker opened or retry exhausted for getDashboardData. Error: " + e.getMessage());
        return Collections.emptyList();
    }
    
    @CircuitBreaker(name = "apiService", fallbackMethod = "saveDailyDataFallback")
    @Retry(name = "apiService")
    public boolean saveDailyData(Map<String, Object> data, String token) {
        // Try gateway first, fallback to direct data service if gateway fails
        String[] urls = {
            gatewayUrl + "/api/data/daily",
            dataServiceUrl + "/api/data/daily"
        };
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
        
        for (String url : urls) {
            try {
                System.out.println("Attempting to save data to: " + url);
                System.out.println("Data: " + data);
                
                ResponseEntity<?> response = restTemplate.postForEntity(url, entity, Object.class);
                System.out.println("Save data response status: " + response.getStatusCode());
                System.out.println("Save data response body: " + response.getBody());
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Data saved successfully via: " + url);
                    return true;
                }
            } catch (HttpClientErrorException e) {
                System.err.println("========================================");
                System.err.println("Save data API call failed with HTTP " + e.getStatusCode());
                System.err.println("URL: " + url);
                System.err.println("Status Code: " + e.getStatusCode());
                System.err.println("Status Text: " + e.getStatusText());
                if (e.getResponseBodyAsString() != null && !e.getResponseBodyAsString().isEmpty()) {
                    System.err.println("Response body: " + e.getResponseBodyAsString());
                }
                System.err.println("========================================");
                
                // If 404 from gateway, try direct data service
                if (e.getStatusCode().value() == 404 && url.contains(gatewayUrl)) {
                    System.out.println("Gateway returned 404, will try direct data service...");
                    continue;
                }
                // If 4xx error other than 404, don't try other URL
                if (e.getStatusCode().value() >= 400 && e.getStatusCode().value() < 500 && e.getStatusCode().value() != 404) {
                    break;
                }
            } catch (RestClientException e) {
                System.err.println("========================================");
                System.err.println("Save data API call failed (network/connection error)");
                System.err.println("URL: " + url);
                System.err.println("Error: " + e.getMessage());
                System.err.println("========================================");
                // Try next URL if this one fails
                continue;
            } catch (Exception e) {
                System.err.println("========================================");
                System.err.println("Save data API call failed (unexpected error)");
                System.err.println("URL: " + url);
                System.err.println("Error: " + e.getMessage());
                System.err.println("========================================");
                // Try next URL if this one fails
                continue;
            }
        }
        return false;
    }
    
    public boolean saveDailyDataFallback(Map<String, Object> data, String token, Exception e) {
        System.err.println("Circuit breaker opened or retry exhausted for saveDailyData. Error: " + e.getMessage());
        return false;
    }
    
    @CircuitBreaker(name = "apiService", fallbackMethod = "getAlertsFallback")
    @Retry(name = "apiService")
    public List<Map<String, Object>> getAlerts(String token, String userId) {
        // Try gateway first, fallback to direct alert service if gateway fails
        String[] urls = {
            gatewayUrl + "/api/alerts" + (userId != null ? "?userId=" + userId : ""),
            alertServiceUrl + "/api/alerts" + (userId != null ? "?userId=" + userId : "")
        };
        
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        for (String url : urls) {
            try {
                System.out.println("Fetching alerts from: " + url);
                ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url, 
                    HttpMethod.GET, 
                    entity, 
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                );
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    System.out.println("Alerts fetched: " + response.getBody().size());
                    return response.getBody();
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 404 && url.contains(gatewayUrl)) {
                    continue; // Try direct service
                }
                System.err.println("Error fetching alerts: " + e.getStatusCode());
            } catch (Exception e) {
                System.err.println("Error fetching alerts: " + e.getMessage());
                if (url.contains(gatewayUrl)) {
                    continue; // Try direct service
                }
            }
        }
        return Collections.emptyList();
    }
    
    public List<Map<String, Object>> getAlertsFallback(String token, String userId, Exception e) {
        System.err.println("Circuit breaker opened or retry exhausted for getAlerts. Error: " + e.getMessage());
        return Collections.emptyList();
    }
    
    @CircuitBreaker(name = "apiService", fallbackMethod = "registerFallback")
    @Retry(name = "apiService")
    public boolean register(String username, String password, String email, String firstName, String lastName) {
        String[] urls = {
            gatewayUrl + "/api/auth/register",
            authServiceUrl + "/api/auth/register"
        };
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, String> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);
        request.put("email", email);
        if (firstName != null) request.put("firstName", firstName);
        if (lastName != null) request.put("lastName", lastName);
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        
        for (String url : urls) {
            try {
                ResponseEntity<?> response = restTemplate.postForEntity(url, entity, Object.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    return true;
                }
            } catch (Exception e) {
                if (url.equals(gatewayUrl + "/api/auth/register")) {
                    continue;
                }
                return false;
            }
        }
        return false;
    }
    
    public boolean registerFallback(String username, String password, String email, String firstName, String lastName, Exception e) {
        System.err.println("Circuit breaker opened or retry exhausted for register. Error: " + e.getMessage());
        return false;
    }
    
    @CircuitBreaker(name = "apiService", fallbackMethod = "getAllUsersFallback")
    @Retry(name = "apiService")
    public List<Map<String, Object>> getAllUsers(String token) {
        String[] urls = {
            gatewayUrl + "/api/auth/users",
            authServiceUrl + "/api/auth/users"
        };
        
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        for (String url : urls) {
            try {
                System.out.println("Fetching users from: " + url);
                ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                );
                System.out.println("Users API response status: " + response.getStatusCode());
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    System.out.println("Users fetched: " + response.getBody().size());
                    return response.getBody();
                } else if (response.getStatusCode().value() == 403) {
                    System.err.println("Access forbidden - user is not admin");
                    throw new RuntimeException("Access denied. Admin privileges required.");
                }
            } catch (HttpClientErrorException e) {
                System.err.println("Error fetching users with HTTP " + e.getStatusCode() + " from " + url);
                if (e.getStatusCode().value() == 403) {
                    throw new RuntimeException("Access denied. Admin privileges required.");
                }
                if (e.getStatusCode().value() == 404 && url.equals(gatewayUrl + "/api/auth/users")) {
                    continue; // Try direct service
                }
                throw new RuntimeException("Failed to fetch users: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error fetching users from " + url + ": " + e.getMessage());
                e.printStackTrace();
                if (url.equals(gatewayUrl + "/api/auth/users")) {
                    continue; // Try direct service
                }
                throw new RuntimeException("Failed to fetch users: " + e.getMessage(), e);
            }
        }
        throw new RuntimeException("Failed to fetch users from all available endpoints");
    }
    
    public List<Map<String, Object>> getAllUsersFallback(String token, Exception e) {
        System.err.println("Circuit breaker opened or retry exhausted for getAllUsers. Error: " + e.getMessage());
        throw new RuntimeException("Failed to fetch users: " + e.getMessage(), e);
    }
    
    @CircuitBreaker(name = "apiService", fallbackMethod = "updateUserFallback")
    @Retry(name = "apiService")
    public boolean updateUser(Long id, Map<String, Object> userData, String token) {
        String[] urls = {
            gatewayUrl + "/api/auth/users/" + id,
            authServiceUrl + "/api/auth/users/" + id
        };
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userData, headers);
        
        for (String url : urls) {
            try {
                ResponseEntity<?> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    Object.class
                );
                return response.getStatusCode().is2xxSuccessful();
            } catch (Exception e) {
                if (url.equals(gatewayUrl + "/api/auth/users/" + id)) {
                    continue;
                }
                return false;
            }
        }
        return false;
    }
    
    public boolean updateUserFallback(Long id, Map<String, Object> userData, String token, Exception e) {
        System.err.println("Circuit breaker opened or retry exhausted for updateUser. Error: " + e.getMessage());
        return false;
    }
    
    @CircuitBreaker(name = "apiService", fallbackMethod = "deleteUserFallback")
    @Retry(name = "apiService")
    public boolean deleteUser(Long id, String token) {
        String[] urls = {
            gatewayUrl + "/api/auth/users/" + id,
            authServiceUrl + "/api/auth/users/" + id
        };
        
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        for (String url : urls) {
            try {
                ResponseEntity<?> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    Object.class
                );
                return response.getStatusCode().is2xxSuccessful();
            } catch (Exception e) {
                if (url.equals(gatewayUrl + "/api/auth/users/" + id)) {
                    continue;
                }
                return false;
            }
        }
        return false;
    }
    
    public boolean deleteUserFallback(Long id, String token, Exception e) {
        System.err.println("Circuit breaker opened or retry exhausted for deleteUser. Error: " + e.getMessage());
        return false;
    }
    
    @CircuitBreaker(name = "apiService", fallbackMethod = "getRecommendationFallback")
    @Retry(name = "apiService")
    public String getRecommendation(String userId, String dataSummary, String token) {
        String[] urls = {
            gatewayUrl + "/api/recommendations/user/" + userId + (dataSummary != null ? "?dataSummary=" + dataSummary : ""),
            recommendationServiceUrl + "/api/recommendations/user/" + userId + (dataSummary != null ? "?dataSummary=" + dataSummary : "")
        };
        
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        for (String url : urls) {
            try {
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
                );
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> body = response.getBody();
                    Object recommendation = body.get("recommendation");
                    return recommendation != null ? recommendation.toString() : "No recommendation available.";
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 404 && url.contains(gatewayUrl)) {
                    continue;
                }
            } catch (Exception e) {
                if (url.contains(gatewayUrl)) {
                    continue;
                }
            }
        }
        return "Unable to fetch recommendations at this time.";
    }
    
    public String getRecommendationFallback(String userId, String dataSummary, String token, Exception e) {
        System.err.println("Circuit breaker opened or retry exhausted for getRecommendation. Error: " + e.getMessage());
        return "Recommendation service is temporarily unavailable.";
    }
}

