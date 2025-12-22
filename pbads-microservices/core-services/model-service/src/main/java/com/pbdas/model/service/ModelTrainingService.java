package com.pbdas.model.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModelTrainingService {
    
    @Value("${model.training.csv-path}")
    private String csvPath;
    
    @Value("${model.training.output-dir:./models/trained}")
    private String outputDir;
    
    @Value("${model.training.algorithm:isolation-forest}")
    private String algorithm;
    
    @Value("${model.training.contamination:0.1}")
    private double contamination;
    
    @Value("${model.training.n-estimators:100}")
    private int nEstimators;
    
    @Value("${model.training.nu:0.1}")
    private double nu;
    
    public Map<String, Object> trainModel() {
        Map<String, Object> result = new HashMap<>();
        List<String> output = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try {
            // Verify CSV file exists
            File csvFile = new File(csvPath);
            if (!csvFile.exists()) {
                result.put("success", false);
                result.put("error", "CSV file not found at: " + csvPath);
                return result;
            }
            
            // Get the Python script path
            String scriptPath = Paths.get("src", "main", "resources", "ml", "scripts", "train_model.py").toString();
            File scriptFile = new File(scriptPath);
            
            // If script not found in current directory, try absolute path
            if (!scriptFile.exists()) {
                scriptPath = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "ml", "scripts", "train_model.py").toString();
                scriptFile = new File(scriptPath);
            }
            
            if (!scriptFile.exists()) {
                result.put("success", false);
                result.put("error", "Training script not found at: " + scriptPath);
                return result;
            }
            
            // Prepare environment variables
            Map<String, String> env = new HashMap<>(System.getenv());
            env.put("CSV_PATH", csvPath);
            env.put("OUTPUT_DIR", outputDir);
            env.put("ALGORITHM", algorithm);
            env.put("CONTAMINATION", String.valueOf(contamination));
            env.put("N_ESTIMATORS", String.valueOf(nEstimators));
            env.put("NU", String.valueOf(nu));
            
            // Create process builder
            ProcessBuilder processBuilder = new ProcessBuilder(
                "python", scriptPath
            );
            
            // Set environment
            processBuilder.environment().putAll(env);
            
            // Set working directory to script's parent directory
            processBuilder.directory(scriptFile.getParentFile());
            
            // Redirect error stream
            processBuilder.redirectErrorStream(true);
            
            output.add("Starting model training...");
            output.add("CSV Path: " + csvPath);
            output.add("Output Directory: " + outputDir);
            output.add("Algorithm: " + algorithm);
            
            // Start process
            Process process = processBuilder.start();
            
            // Read output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.add(line);
                    System.out.println("[Training] " + line);
                }
            }
            
            // Wait for process to complete
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                result.put("success", true);
                result.put("message", "Model training completed successfully");
                result.put("output", output);
            } else {
                result.put("success", false);
                result.put("error", "Training process exited with code: " + exitCode);
                result.put("output", output);
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Error during training: " + e.getMessage());
            errors.add(e.getMessage());
            e.printStackTrace();
        }
        
        result.put("errors", errors);
        return result;
    }
    
    public String getCsvPath() {
        return csvPath;
    }
    
    public boolean csvFileExists() {
        return new File(csvPath).exists();
    }
}
