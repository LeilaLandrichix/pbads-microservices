package com.pbdas.monitoring.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceApplication {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("instance")
    private List<ServiceInstance> instances;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ServiceInstance> getInstances() {
        return instances;
    }

    public void setInstances(List<ServiceInstance> instances) {
        this.instances = instances;
    }
}

