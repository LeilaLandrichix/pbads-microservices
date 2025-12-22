package com.pbdas.data.repository;

import com.pbdas.data.model.entity.DailyData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyDataRepository extends JpaRepository<DailyData, Long> {
    
    List<DailyData> findByUserId(String userId);
    
    Optional<DailyData> findByUserIdAndDate(String userId, LocalDate date);
    
    List<DailyData> findByUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT d FROM DailyData d WHERE d.userId = :userId ORDER BY d.date DESC")
    List<DailyData> findRecentByUserId(@Param("userId") String userId);
    
    @Query("SELECT COUNT(d) FROM DailyData d WHERE d.userId = :userId")
    Long countByUserId(@Param("userId") String userId);
    
    boolean existsByUserIdAndDate(String userId, LocalDate date);
    
    void deleteByUserIdAndDate(String userId, LocalDate date);
}
