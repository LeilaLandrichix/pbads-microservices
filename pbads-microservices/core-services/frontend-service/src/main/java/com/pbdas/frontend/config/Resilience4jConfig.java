package com.pbdas.frontend.config;

import io.github.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import io.github.resilience4j.retry.event.RetryEvent;
import io.github.resilience4j.retry.event.RetryOnRetryEvent;
import io.github.resilience4j.retry.event.RetryOnErrorEvent;
import io.github.resilience4j.retry.event.RetryOnSuccessEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class Resilience4jConfig {
    
    @EventListener
    public void onRetryEvent(RetryEvent event) {
        if (event instanceof RetryOnRetryEvent) {
            RetryOnRetryEvent retryEvent = (RetryOnRetryEvent) event;
            System.err.println("========================================");
            System.err.println("[Resilience4j Retry] ⚠️  RETRY ATTEMPT #" + retryEvent.getNumberOfRetryAttempts());
            System.err.println("[Resilience4j Retry] Method: " + retryEvent.getName());
            System.err.println("[Resilience4j Retry] Error: " + retryEvent.getLastThrowable().getMessage());
            System.err.println("[Resilience4j Retry] Waiting before next retry...");
            System.err.println("========================================");
        } else if (event instanceof RetryOnErrorEvent) {
            RetryOnErrorEvent errorEvent = (RetryOnErrorEvent) event;
            System.err.println("========================================");
            System.err.println("[Resilience4j Retry] ❌ ALL RETRY ATTEMPTS EXHAUSTED");
            System.err.println("[Resilience4j Retry] Method: " + errorEvent.getName());
            System.err.println("[Resilience4j Retry] Total attempts: " + errorEvent.getNumberOfRetryAttempts());
            System.err.println("[Resilience4j Retry] Final error: " + errorEvent.getLastThrowable().getMessage());
            System.err.println("[Resilience4j Retry] Now triggering fallback method...");
            System.err.println("========================================");
        } else if (event instanceof RetryOnSuccessEvent) {
            RetryOnSuccessEvent successEvent = (RetryOnSuccessEvent) event;
            System.out.println("========================================");
            System.out.println("[Resilience4j Retry] ✅ SUCCESS after " + successEvent.getNumberOfRetryAttempts() + " retry attempts");
            System.out.println("[Resilience4j Retry] Method: " + successEvent.getName());
            System.out.println("========================================");
        }
    }
    
    @EventListener
    public void onCircuitBreakerEvent(CircuitBreakerEvent event) {
        // Log circuit breaker events (simplified to avoid API compatibility issues)
        System.err.println("========================================");
        System.err.println("[Resilience4j Circuit Breaker] 🔄 Event: " + event.getEventType());
        System.err.println("[Resilience4j Circuit Breaker] Instance: " + event.getCircuitBreakerName());
        System.err.println("========================================");
    }
}

