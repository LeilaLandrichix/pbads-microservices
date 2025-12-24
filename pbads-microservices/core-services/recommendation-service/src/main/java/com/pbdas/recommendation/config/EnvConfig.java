package com.pbdas.recommendation.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class EnvConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(EnvConfig.class);
    
    @PostConstruct
    public void loadEnvFile() {
        try {
            // Try multiple locations for .env file - normalize to absolute paths
            String currentDir = System.getProperty("user.dir");
            String[] possiblePaths;
            try {
                possiblePaths = new String[]{
                    currentDir, // Current directory (recommendation-service)
                    new File(currentDir, "..").getCanonicalPath(), // core-services
                    new File(currentDir, ".." + File.separator + "..").getCanonicalPath(), // pbads-microservices root
                };
            } catch (Exception e) {
                possiblePaths = new String[]{
                    currentDir,
                    currentDir + File.separator + "..",
                    currentDir + File.separator + ".." + File.separator + "..",
                };
            }
            
            Dotenv dotenv = null;
            String loadedFrom = null;
            
            for (String path : possiblePaths) {
                try {
                    File dir = new File(path);
                    File envFile = new File(dir, ".env");
                    logger.debug("Checking for .env at: {}", envFile.getAbsolutePath());
                    if (envFile.exists() && envFile.isFile()) {
                        dotenv = Dotenv.configure()
                            .directory(path)
                            .ignoreIfMissing()
                            .load();
                        loadedFrom = envFile.getAbsolutePath();
                        logger.info("Found .env file at: {}", loadedFrom);
                        break;
                    }
                } catch (Exception e) {
                    logger.debug("Error checking path {}: {}", path, e.getMessage());
                    // Try next path
                    continue;
                }
            }
            
            if (dotenv != null) {
                // Set environment variables from .env file
                dotenv.entries().forEach(entry -> {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    // Remove quotes if present
                    if (value != null && value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    if (System.getenv(key) == null) {
                        System.setProperty(key, value);
                    }
                });
                
                // Specifically check GEMINI_API_KEY
                String geminiKey = dotenv.get("GEMINI_API_KEY");
                if (geminiKey != null) {
                    // Remove quotes if present
                    if (geminiKey.startsWith("\"") && geminiKey.endsWith("\"")) {
                        geminiKey = geminiKey.substring(1, geminiKey.length() - 1);
                    }
                    if (System.getenv("GEMINI_API_KEY") == null) {
                        System.setProperty("GEMINI_API_KEY", geminiKey);
                    }
                    logger.info("✓ Loaded GEMINI_API_KEY from .env file: {}", loadedFrom);
                } else {
                    logger.warn("⚠ GEMINI_API_KEY not found in .env file at: {}", loadedFrom);
                }
            } else {
                logger.warn("⚠ .env file not found in any of the checked locations");
            }
        } catch (Exception e) {
            logger.error("Error loading .env file: {}", e.getMessage());
        }
    }
}

