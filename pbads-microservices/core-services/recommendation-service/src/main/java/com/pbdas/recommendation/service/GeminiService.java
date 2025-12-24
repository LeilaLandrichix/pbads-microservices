package com.pbdas.recommendation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class GeminiService {
    
    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    
    @Value("${gemini.api.key:${GEMINI_API_KEY:}}")
    private String apiKey;
    
    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String modelName;
    
    @Value("${gemini.api.timeout:30000}")
    private int timeout;
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
    
    public GeminiService() {
        this.webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public String generateRecommendation(String userDataSummary) {
        try {
            // Check API key - try multiple sources
            String key = null;
            
            // 1. Try from @Value injection (application.yml)
            if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("${GEMINI_API_KEY:}")) {
                key = apiKey;
                logger.debug("Using API key from application properties");
            }
            
            // 2. Try from system property (set by dotenv)
            if ((key == null || key.isEmpty()) && System.getProperty("GEMINI_API_KEY") != null) {
                key = System.getProperty("GEMINI_API_KEY");
                logger.debug("Using API key from system property");
            }
            
            // 3. Try from environment variable
            if ((key == null || key.isEmpty()) && System.getenv("GEMINI_API_KEY") != null) {
                key = System.getenv("GEMINI_API_KEY");
                logger.debug("Using API key from environment variable");
            }
            
            if (key == null || key.isEmpty()) {
                logger.error("GEMINI_API_KEY is not set. Checked: application properties, system properties, environment variables");
                logger.error("Please set GEMINI_API_KEY in .env file or as environment variable");
                return "Recommendation service is not configured. Please set GEMINI_API_KEY environment variable.";
            }
            
            logger.info("Using Gemini API key (length: {})", key.length());
            
            // Build prompt for recommendations
            String prompt = buildRecommendationPrompt(userDataSummary);
            
            logger.info("Calling Gemini API with model: {}", modelName);
            
            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            content.put("parts", new Object[]{part});
            requestBody.put("contents", new Object[]{content});
            
            // Make API call
            String url = String.format("/v1beta/models/%s:generateContent?key=%s", modelName, key);
            
            String responseJson = webClient.post()
                .uri(url)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            // Parse response
            JsonNode responseNode = objectMapper.readTree(responseJson);
            JsonNode candidates = responseNode.get("candidates");
            
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode contentNode = firstCandidate.get("content");
                if (contentNode != null) {
                    JsonNode parts = contentNode.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        JsonNode textNode = parts.get(0).get("text");
                        if (textNode != null) {
                            String recommendation = textNode.asText();
                            logger.info("Received recommendation from Gemini");
                            return recommendation;
                        }
                    }
                }
            }
            
            logger.warn("Unexpected response format from Gemini API");
            return "Unable to parse recommendation from AI service.";
            
        } catch (Exception e) {
            logger.error("Error generating recommendation from Gemini", e);
            return "Unable to generate recommendation at this time. Please try again later.";
        }
    }
    
    private String buildRecommendationPrompt(String userDataSummary) {
        return "You are a personal health and wellness advisor. Based on the following user's daily habit data, " +
               "provide personalized, actionable recommendations to improve their well-being. " +
               "Focus on sleep, physical activity, hydration, study habits, and mood. " +
               "Keep recommendations concise (2-3 sentences) and practical.\n\n" +
               "User Data Summary:\n" + userDataSummary + "\n\n" +
               "Recommendations:";
    }
}

