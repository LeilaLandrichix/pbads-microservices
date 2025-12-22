package com.pbdas.data.controller;

import com.pbdas.data.model.dto.DailyDataDTO;
import com.pbdas.data.service.DailyDataService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "*")
public class DailyDataController {
    
    @Autowired
    private DailyDataService dailyDataService;
    
    @PostMapping("/daily")
    public ResponseEntity<DailyDataDTO> createDailyData(@Valid @RequestBody DailyDataDTO dto) {
        DailyDataDTO created = dailyDataService.createDailyData(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/daily/{id}")
    public ResponseEntity<DailyDataDTO> getDailyDataById(@PathVariable Long id) {
        return dailyDataService.getDailyDataById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/daily/user/{userId}")
    public ResponseEntity<List<DailyDataDTO>> getDailyDataByUserId(@PathVariable String userId) {
        List<DailyDataDTO> data = dailyDataService.getDailyDataByUserId(userId);
        return ResponseEntity.ok(data);
    }
    
    @GetMapping("/daily/user/{userId}/date/{date}")
    public ResponseEntity<DailyDataDTO> getDailyDataByUserAndDate(
            @PathVariable String userId,
            @PathVariable LocalDate date) {
        return dailyDataService.getDailyDataByUserAndDate(userId, date)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/daily/user/{userId}/range")
    public ResponseEntity<List<DailyDataDTO>> getDailyDataByDateRange(
            @PathVariable String userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<DailyDataDTO> data = dailyDataService.getDailyDataByUserIdAndDateRange(
            userId, startDate, endDate);
        return ResponseEntity.ok(data);
    }
    
    @PutMapping("/daily/{id}")
    public ResponseEntity<DailyDataDTO> updateDailyData(
            @PathVariable Long id,
            @Valid @RequestBody DailyDataDTO dto) {
        try {
            DailyDataDTO updated = dailyDataService.updateDailyData(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/daily/{id}")
    public ResponseEntity<Void> deleteDailyData(@PathVariable Long id) {
        dailyDataService.deleteDailyData(id);
        return ResponseEntity.noContent().build();
    }
}
