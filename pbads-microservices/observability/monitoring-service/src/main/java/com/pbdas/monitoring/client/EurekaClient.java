package com.pbdas.monitoring.client;

import com.pbdas.monitoring.model.dto.ServiceApplication;
import com.pbdas.monitoring.model.dto.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EurekaClient {
    
    private static final Logger logger = LoggerFactory.getLogger(EurekaClient.class);
    
    private final DiscoveryClient discoveryClient;
    
    public EurekaClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }
    
    /**
     * Récupère tous les services enregistrés dans Eureka
     */
    public List<ServiceApplication> getAllServices() {
        try {
            List<String> serviceNames = discoveryClient.getServices();
            logger.info("Found {} services in Eureka", serviceNames.size());
            
            List<ServiceApplication> services = new ArrayList<>();
            for (String serviceName : serviceNames) {
                ServiceApplication service = getService(serviceName);
                if (service != null) {
                    services.add(service);
                }
            }
            
            return services;
            
        } catch (Exception e) {
            logger.error("Error fetching services from Eureka: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Récupère un service spécifique par son nom
     */
    public ServiceApplication getService(String serviceName) {
        try {
            List<org.springframework.cloud.client.ServiceInstance> instances = 
                discoveryClient.getInstances(serviceName);
            
            if (instances == null || instances.isEmpty()) {
                return null;
            }
            
            ServiceApplication service = new ServiceApplication();
            service.setName(serviceName);
            
            List<ServiceInstance> serviceInstances = instances.stream()
                .map(this::convertToServiceInstance)
                .collect(Collectors.toList());
            
            service.setInstances(serviceInstances);
            return service;
            
        } catch (Exception e) {
            logger.error("Error fetching service {} from Eureka: {}", serviceName, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Convertit une ServiceInstance de Spring Cloud en ServiceInstance DTO
     */
    private ServiceInstance convertToServiceInstance(org.springframework.cloud.client.ServiceInstance instance) {
        ServiceInstance dto = new ServiceInstance();
        dto.setInstanceId(instance.getInstanceId());
        dto.setHostName(instance.getHost());
        dto.setIpAddr(instance.getHost());
        dto.setApp(instance.getServiceId());
        dto.setStatus("UP"); // Spring Cloud DiscoveryClient ne retourne que les instances UP
        
        // Créer un objet Port
        ServiceInstance.Port port = new ServiceInstance.Port();
        port.setPort(instance.getPort());
        port.setEnabled("true");
        dto.setPort(port);
        
        // Construire les URLs
        String scheme = instance.getScheme() != null ? instance.getScheme() : "http";
        String baseUrl = scheme + "://" + instance.getHost() + ":" + instance.getPort();
        dto.setHomePageUrl(baseUrl);
        dto.setHealthCheckUrl(baseUrl + "/actuator/health");
        dto.setStatusPageUrl(baseUrl + "/actuator/info");
        
        return dto;
    }
    
    /**
     * Récupère toutes les instances de tous les services
     */
    public List<ServiceInstance> getAllInstances() {
        List<ServiceApplication> services = getAllServices();
        return services.stream()
            .flatMap(app -> app.getInstances() != null ? app.getInstances().stream() : java.util.stream.Stream.empty())
            .collect(Collectors.toList());
    }
    
    /**
     * Récupère les instances d'un service spécifique
     */
    public List<ServiceInstance> getServiceInstances(String serviceName) {
        ServiceApplication service = getService(serviceName);
        if (service != null && service.getInstances() != null) {
            return service.getInstances();
        }
        return new ArrayList<>();
    }
}
