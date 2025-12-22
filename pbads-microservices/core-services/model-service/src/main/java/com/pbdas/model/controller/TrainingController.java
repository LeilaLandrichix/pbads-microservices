package com.pbdas.model.controller;

import com.pbdas.model.service.ModelTrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/model")
@CrossOrigin(origins = "*")
public class TrainingController {
    
    @Autowired
    private ModelTrainingService modelTrainingService;
    
    @PostMapping("/train")
    public ResponseEntity<Map<String, Object>> trainModel() {
        // Check if CSV file exists
        if (!modelTrainingService.csvFileExists()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "CSV file not found at: " + modelTrainingService.getCsvPath()
            ));
        }
        
        Map<String, Object> result = modelTrainingService.trainModel();
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(500).body(result);
        }
    }
    
    @GetMapping("/train/status")
    public ResponseEntity<Map<String, Object>> getTrainingStatus() {
        boolean csvExists = modelTrainingService.csvFileExists();
        String csvPath = modelTrainingService.getCsvPath();
        
        return ResponseEntity.ok(Map.of(
            "csvPath", csvPath,
            "csvExists", csvExists,
            "ready", csvExists
        ));
    }
}
