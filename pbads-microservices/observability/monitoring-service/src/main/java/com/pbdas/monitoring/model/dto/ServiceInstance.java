package com.pbdas.monitoring.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceInstance {
    
    @JsonProperty("instanceId")
    private String instanceId;
    
    @JsonProperty("hostName")
    private String hostName;
    
    @JsonProperty("app")
    private String app;
    
    @JsonProperty("ipAddr")
    private String ipAddr;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("port")
    private Port port;
    
    @JsonProperty("homePageUrl")
    private String homePageUrl;
    
    @JsonProperty("healthCheckUrl")
    private String healthCheckUrl;
    
    @JsonProperty("statusPageUrl")
    private String statusPageUrl;

    // Getters and Setters
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Port getPort() {
        return port;
    }

    public void setPort(Port port) {
        this.port = port;
    }

    public String getHomePageUrl() {
        return homePageUrl;
    }

    public void setHomePageUrl(String homePageUrl) {
        this.homePageUrl = homePageUrl;
    }

    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }

    public void setHealthCheckUrl(String healthCheckUrl) {
        this.healthCheckUrl = healthCheckUrl;
    }

    public String getStatusPageUrl() {
        return statusPageUrl;
    }

    public void setStatusPageUrl(String statusPageUrl) {
        this.statusPageUrl = statusPageUrl;
    }
    
    public String getBaseUrl() {
        if (port != null && port.getPort() != null) {
            return "http://" + (ipAddr != null ? ipAddr : hostName) + ":" + port.getPort();
        }
        return null;
    }
    
    public String getActuatorHealthUrl() {
        String baseUrl = getBaseUrl();
        return baseUrl != null ? baseUrl + "/actuator/health" : null;
    }
    
    public String getActuatorInfoUrl() {
        String baseUrl = getBaseUrl();
        return baseUrl != null ? baseUrl + "/actuator/info" : null;
    }
    
    public String getActuatorMetricsUrl() {
        String baseUrl = getBaseUrl();
        return baseUrl != null ? baseUrl + "/actuator/metrics" : null;
    }
    
    public String getActuatorPrometheusUrl() {
        String baseUrl = getBaseUrl();
        return baseUrl != null ? baseUrl + "/actuator/prometheus" : null;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Port {
        @JsonProperty("$")
        private Integer port;
        
        @JsonProperty("@enabled")
        private String enabled;

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getEnabled() {
            return enabled;
        }

        public void setEnabled(String enabled) {
            this.enabled = enabled;
        }
    }
}

