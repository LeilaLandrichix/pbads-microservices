package com.pbdas.recommendation;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class RecommendationApplication {

	public static void main(String[] args) {
		// Load .env file if it exists
		// Try multiple locations: current directory, parent directory (pbads-microservices root)
		try {
			Dotenv dotenv = null;
			String loadedFrom = null;
			
			// Try multiple paths - normalize them to absolute paths
			String currentDir = System.getProperty("user.dir");
			String[] pathsToTry = {
				currentDir, // Current directory (recommendation-service)
				new File(currentDir, "..").getCanonicalPath(), // core-services
				new File(currentDir, ".." + File.separator + "..").getCanonicalPath(), // pbads-microservices root
			};
			
			for (String path : pathsToTry) {
				try {
					File envFile = new File(path, ".env");
					if (envFile.exists()) {
						dotenv = Dotenv.configure()
							.directory(path)
							.ignoreIfMissing()
							.load();
						loadedFrom = envFile.getAbsolutePath();
						System.out.println("Found .env file at: " + loadedFrom);
						break;
					}
				} catch (Exception e) {
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
				
				// Specifically set GEMINI_API_KEY if not already set
				String geminiKey = dotenv.get("GEMINI_API_KEY");
				if (geminiKey != null) {
					// Remove quotes if present
					if (geminiKey.startsWith("\"") && geminiKey.endsWith("\"")) {
						geminiKey = geminiKey.substring(1, geminiKey.length() - 1);
					}
					if (System.getenv("GEMINI_API_KEY") == null) {
						System.setProperty("GEMINI_API_KEY", geminiKey);
					}
					System.out.println("✓ Loaded GEMINI_API_KEY from .env file: " + loadedFrom);
				} else {
					System.out.println("⚠ GEMINI_API_KEY not found in .env file at: " + loadedFrom);
				}
			} else {
				System.out.println("⚠ .env file not found. Checked paths:");
				for (String path : pathsToTry) {
					System.out.println("  - " + new File(path, ".env").getAbsolutePath());
				}
			}
		} catch (Exception e) {
			System.out.println("Note: .env file not found or couldn't be loaded. Using system environment variables.");
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
		
		SpringApplication.run(RecommendationApplication.class, args);
	}

}

