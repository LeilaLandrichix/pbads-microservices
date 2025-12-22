package com.pbdas.alert.repository;

import com.pbdas.alert.model.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    List<Alert> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<Alert> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, String status);
    
    List<Alert> findByStatusOrderByCreatedAtDesc(String status);
    
    Optional<Alert> findByUserIdAndDate(String userId, String date);
    
    long countByUserIdAndStatus(String userId, String status);
}
