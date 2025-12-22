package com.pbdas.data.mapper;

import com.pbdas.data.model.dto.DailyDataDTO;
import com.pbdas.data.model.entity.DailyData;
import org.springframework.stereotype.Component;

@Component
public class DataMapper {
    
    public DailyData toEntity(DailyDataDTO dto) {
        if (dto == null) {
            return null;
        }
        
        DailyData entity = new DailyData();
        entity.setId(dto.getId());
        entity.setUserId(dto.getUserId());
        entity.setDate(dto.getDate());
        entity.setWakeUpTime(dto.getWakeUpTime());
        entity.setSleepHours(dto.getSleepHours());
        entity.setSteps(dto.getSteps());
        entity.setCaloriesBurned(dto.getCaloriesBurned());
        entity.setWaterIntakeMl(dto.getWaterIntakeMl());
        entity.setStudyHours(dto.getStudyHours());
        entity.setMoodScore(dto.getMoodScore());
        
        return entity;
    }
    
    public DailyDataDTO toDTO(DailyData entity) {
        if (entity == null) {
            return null;
        }
        
        DailyDataDTO dto = new DailyDataDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setDate(entity.getDate());
        dto.setWakeUpTime(entity.getWakeUpTime());
        dto.setSleepHours(entity.getSleepHours());
        dto.setSteps(entity.getSteps());
        dto.setCaloriesBurned(entity.getCaloriesBurned());
        dto.setWaterIntakeMl(entity.getWaterIntakeMl());
        dto.setStudyHours(entity.getStudyHours());
        dto.setMoodScore(entity.getMoodScore());
        
        return dto;
    }
}
