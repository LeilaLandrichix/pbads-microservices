package com.pbdas.monitoring.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EurekaApplications {
    
    @JsonProperty("applications")
    private ApplicationsWrapper applications;

    public ApplicationsWrapper getApplications() {
        return applications;
    }

    public void setApplications(ApplicationsWrapper applications) {
        this.applications = applications;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApplicationsWrapper {
        @JsonProperty("application")
        private List<ServiceApplication> applications;

        public List<ServiceApplication> getApplications() {
            return applications;
        }

        public void setApplications(List<ServiceApplication> applications) {
            this.applications = applications;
        }
    }
}

