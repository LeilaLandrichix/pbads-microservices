package com.pbdas.monitoring.controller;

import com.pbdas.monitoring.model.dto.ServiceApplication;
import com.pbdas.monitoring.model.dto.ServiceInstance;
import com.pbdas.monitoring.service.MetricsCollectorService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/monitoring")
public class MetricsController {
    
    private final MetricsCollectorService metricsCollectorService;
    
    public MetricsController(MetricsCollectorService metricsCollectorService) {
        this.metricsCollectorService = metricsCollectorService;
    }
    
    /**
     * Page d'accueil - redirige vers la liste des services
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/api/monitoring/services";
    }
    
    /**
     * Page principale listant tous les services avec leurs liens actuator (HTML)
     */
    @GetMapping("/services")
    public String getServices(Model model) {
        Map<String, List<ServiceInstance>> services = metricsCollectorService.getAllServicesWithInstances();
        model.addAttribute("services", services);
        return "services";
    }
    
    /**
     * Détails d'un service spécifique (HTML)
     */
    @GetMapping("/services/{serviceName}")
    public String getServiceDetails(@PathVariable String serviceName, Model model) {
        ServiceApplication service = metricsCollectorService.getServiceInfo(serviceName);
        model.addAttribute("service", service);
        return "service-details";
    }
}

/**
 * Contrôleur REST séparé pour les endpoints JSON
 */
@RestController
@RequestMapping("/api/monitoring/api")
class MetricsRestController {
    
    private final MetricsCollectorService metricsCollectorService;
    
    public MetricsRestController(MetricsCollectorService metricsCollectorService) {
        this.metricsCollectorService = metricsCollectorService;
    }
    
    /**
     * API REST pour obtenir tous les services (JSON)
     */
    @GetMapping("/services")
    public ResponseEntity<Map<String, List<ServiceInstance>>> getServicesJson() {
        Map<String, List<ServiceInstance>> services = metricsCollectorService.getAllServicesWithInstances();
        return ResponseEntity.ok(services);
    }
    
    /**
     * API REST pour obtenir un service spécifique (JSON)
     */
    @GetMapping("/services/{serviceName}")
    public ResponseEntity<ServiceApplication> getServiceJson(@PathVariable String serviceName) {
        ServiceApplication service = metricsCollectorService.getServiceInfo(serviceName);
        if (service != null) {
            return ResponseEntity.ok(service);
        }
        return ResponseEntity.notFound().build();
    }
}
