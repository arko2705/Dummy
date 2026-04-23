package com.Dummy.demo.service.externalDependency.resilience;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.Dummy.demo.service.Simulation.SimulationToggle;

@Component
public class circuitBreaker {
    @Autowired
    private SimulationToggle toggle;

    private static class CircuitState {
        int failureCount = 0;
        int successCount = 0;
        long lastFailureTime = 0;
        boolean isOpen = false;
        boolean isHalfOpen = false;

        // Helper method to calculate failure rate percentage
        synchronized double getFailureRate() {
            int total = failureCount + successCount;
            if (total == 0)
                return 0.0;
            return (double) failureCount / total * 100;
        }

        // Reset counters (keep lastFailureTime)
        synchronized void resetCounters() {
            failureCount = 0;
            successCount = 0;
        }
    }

    private final ConcurrentHashMap<String, CircuitState> map = new ConcurrentHashMap<>();

    private static final double FAILURE_RATE_THRESHOLD = 50.0; // 50% failure rate triggers OPEN
    private static final int MINIMUM_REQUESTS = 5; // Need at least 5 requests before calculating rate
    private static final long OPEN_DURATION_MS = 5000; // 5 seconds

    public boolean allowRequest(String dependency) {
        if (!toggle.FailureisEnabled())// 👈 don’t track
            return true;
        // FIX: Add state if missing to prevent NPE on first use
        map.putIfAbsent(dependency, new CircuitState());
        CircuitState state = map.get(dependency);

        synchronized (state) {
            if (state.isOpen) {
                long now = System.currentTimeMillis();
                if (now - state.lastFailureTime > OPEN_DURATION_MS) {
                    state.isOpen = false;
                    state.isHalfOpen = true; // ← Enter HALF-OPEN
                    state.resetCounters(); // Reset counters (keep lastFailureTime)
                    return true; // Allow ONLY this request
                }
                return false;
            }

            if (state.isHalfOpen) {
                return false; // ← BLOCK all other requests while testing
            }

            return true; // CLOSED - all requests allowed
        }
    }

    public void recordSuccess(String dependency) {
        CircuitState state = map.get(dependency);
        if (state != null) {// in case somebody called some bs externalDep. We dont do putIfAbsent bcz
                            // alloweRequest does it for us,which returns true or false and then we call
                            // recordSuccess and failure depending on what it returns
            synchronized (state) {
                if (state.isHalfOpen) {
                    state.isHalfOpen = false; // ← Exit HALF-OPEN, back to CLOSED
                }
                state.successCount++;
            }
        }
    }

    public void recordFailure(String dependency) {
        if (!toggle.FailureisEnabled())// 👈 don’t record
            map.putIfAbsent(dependency, new CircuitState());
        CircuitState state = map.get(dependency);
        synchronized (state) {
            if (state.isHalfOpen) {
                state.isHalfOpen = false;
                state.isOpen = true; // ← Failed trial! Back to OPEN
                state.lastFailureTime = System.currentTimeMillis();
                return; // Don't count this failure twice
            }
            state.failureCount++;
            state.lastFailureTime = System.currentTimeMillis();

            // Check if we have enough requests and failure rate exceeds threshold
            int totalRequests = state.failureCount + state.successCount;
            if (totalRequests >= MINIMUM_REQUESTS && state.getFailureRate() >= FAILURE_RATE_THRESHOLD) {
                state.isOpen = true;
            }
        }
    }

    // Optional: Method to get current failure rate for monitoring
    public double getFailureRate(String dependency) {
        CircuitState state = map.get(dependency);
        if (state == null)
            return 0.0;
        synchronized (state) {
            return state.getFailureRate();
        }
    }
}