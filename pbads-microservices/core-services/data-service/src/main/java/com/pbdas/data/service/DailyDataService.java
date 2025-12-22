package com.pbdas.data.service;

import com.pbdas.data.model.dto.DailyDataDTO;
import com.pbdas.data.model.entity.DailyData;
import com.pbdas.data.model.event.DataLoggedEvent;
import com.pbdas.data.repository.DailyDataRepository;
import com.pbdas.data.kafka.producer.DataEventProducer;
import com.pbdas.data.mapper.DataMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DailyDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(DailyDataService.class);
    
    @Autowired
    private DailyDataRepository dailyDataRepository;
    
    @Autowired
    private DataEventProducer dataEventProducer;
    
    @Autowired
    private InferenceServiceClient inferenceServiceClient;
    
    @Autowired
    private DataMapper dataMapper;
    
    @Transactional
    public DailyDataDTO createDailyData(DailyDataDTO dto) {
        // Check if data already exists for this user and date
        Optional<DailyData> existing = dailyDataRepository.findByUserIdAndDate(
            dto.getUserId(), dto.getDate()
        );
        
        DailyData dailyData;
        if (existing.isPresent()) {
            dailyData = existing.get();
            updateDailyDataFromDTO(dailyData, dto);
        } else {
            dailyData = dataMapper.toEntity(dto);
        }
        
        dailyData = dailyDataRepository.save(dailyData);
        
        // Publish event to Kafka for inference service
        DataLoggedEvent event = new DataLoggedEvent();
        event.setUserId(dailyData.getUserId());
        event.setDate(dailyData.getDate());
        event.setSleepHours(dailyData.getSleepHours());
        event.setSteps(dailyData.getSteps());
        event.setCaloriesBurned(dailyData.getCaloriesBurned());
        event.setWaterIntakeMl(dailyData.getWaterIntakeMl());
        event.setStudyHours(dailyData.getStudyHours());
        event.setMoodScore(dailyData.getMoodScore());
        event.setWakeUpTime(dailyData.getWakeUpTime());
        
        // Always try direct HTTP call (works even if Kafka is not running)
        inferenceServiceClient.processDataEventDirectly(event);
        
        // Also try Kafka (non-blocking, won't fail if Kafka is down)
        try {
            dataEventProducer.sendDataLoggedEvent(event);
        } catch (Exception e) {
            logger.debug("Kafka event send failed (non-critical): {}", e.getMessage());
        }
        
        return dataMapper.toDTO(dailyData);
    }
    
    public Optional<DailyDataDTO> getDailyDataById(Long id) {
        return dailyDataRepository.findById(id)
            .map(dataMapper::toDTO);
    }
    
    public Optional<DailyDataDTO> getDailyDataByUserAndDate(String userId, LocalDate date) {
        return dailyDataRepository.findByUserIdAndDate(userId, date)
            .map(dataMapper::toDTO);
    }
    
    public List<DailyDataDTO> getDailyDataByUserId(String userId) {
        return dailyDataRepository.findByUserId(userId).stream()
            .map(dataMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    public List<DailyDataDTO> getDailyDataByUserIdAndDateRange(
            String userId, LocalDate startDate, LocalDate endDate) {
        return dailyDataRepository.findByUserIdAndDateBetween(userId, startDate, endDate).stream()
            .map(dataMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public DailyDataDTO updateDailyData(Long id, DailyDataDTO dto) {
        DailyData dailyData = dailyDataRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Daily data not found with id: " + id));
        
        updateDailyDataFromDTO(dailyData, dto);
        dailyData = dailyDataRepository.save(dailyData);
        
        // Publish update event
        DataLoggedEvent event = new DataLoggedEvent();
        event.setUserId(dailyData.getUserId());
        event.setDate(dailyData.getDate());
        event.setSleepHours(dailyData.getSleepHours());
        event.setSteps(dailyData.getSteps());
        event.setCaloriesBurned(dailyData.getCaloriesBurned());
        event.setWaterIntakeMl(dailyData.getWaterIntakeMl());
        event.setStudyHours(dailyData.getStudyHours());
        event.setMoodScore(dailyData.getMoodScore());
        event.setWakeUpTime(dailyData.getWakeUpTime());
        
        // Always try direct HTTP call (works even if Kafka is not running)
        inferenceServiceClient.processDataEventDirectly(event);
        
        // Also try Kafka (non-blocking, won't fail if Kafka is down)
        try {
            dataEventProducer.sendDataLoggedEvent(event);
        } catch (Exception e) {
            logger.debug("Kafka event send failed (non-critical): {}", e.getMessage());
        }
        
        return dataMapper.toDTO(dailyData);
    }
    
    @Transactional
    public void deleteDailyData(Long id) {
        dailyDataRepository.deleteById(id);
    }
    
    private void updateDailyDataFromDTO(DailyData entity, DailyDataDTO dto) {
        if (dto.getWakeUpTime() != null) entity.setWakeUpTime(dto.getWakeUpTime());
        if (dto.getSleepHours() != null) entity.setSleepHours(dto.getSleepHours());
        if (dto.getSteps() != null) entity.setSteps(dto.getSteps());
        if (dto.getCaloriesBurned() != null) entity.setCaloriesBurned(dto.getCaloriesBurned());
        if (dto.getWaterIntakeMl() != null) entity.setWaterIntakeMl(dto.getWaterIntakeMl());
        if (dto.getStudyHours() != null) entity.setStudyHours(dto.getStudyHours());
        if (dto.getMoodScore() != null) entity.setMoodScore(dto.getMoodScore());
    }
}
