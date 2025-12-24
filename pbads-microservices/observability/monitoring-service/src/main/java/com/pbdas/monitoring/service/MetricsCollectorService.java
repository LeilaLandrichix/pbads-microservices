package com.pbdas.monitoring.service;

import com.pbdas.monitoring.client.EurekaClient;
import com.pbdas.monitoring.model.dto.ServiceApplication;
import com.pbdas.monitoring.model.dto.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetricsCollectorService {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricsCollectorService.class);
    
    private final EurekaClient eurekaClient;
    
    public MetricsCollectorService(EurekaClient eurekaClient) {
        this.eurekaClient = eurekaClient;
    }
    
    /**
     * Récupère tous les services avec leurs instances et URLs actuator
     */
    public Map<String, List<ServiceInstance>> getAllServicesWithInstances() {
        Map<String, List<ServiceInstance>> servicesMap = new HashMap<>();
        
        try {
            List<ServiceApplication> services = eurekaClient.getAllServices();
            
            for (ServiceApplication service : services) {
                if (service.getInstances() != null && !service.getInstances().isEmpty()) {
                    // Filtrer uniquement les instances UP
                    List<ServiceInstance> upInstances = service.getInstances().stream()
                        .filter(instance -> "UP".equalsIgnoreCase(instance.getStatus()))
                        .toList();
                    
                    if (!upInstances.isEmpty()) {
                        servicesMap.put(service.getName(), upInstances);
                    }
                }
            }
            
            logger.info("Collected {} services with instances", servicesMap.size());
            
        } catch (Exception e) {
            logger.error("Error collecting services: {}", e.getMessage(), e);
        }
        
        return servicesMap;
    }
    
    /**
     * Récupère les informations d'un service spécifique
     */
    public ServiceApplication getServiceInfo(String serviceName) {
        return eurekaClient.getService(serviceName);
    }
}
