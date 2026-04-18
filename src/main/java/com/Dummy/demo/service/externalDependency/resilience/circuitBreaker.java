package com.Dummy.demo.service.externalDependency.resilience;

import java.util.concurrent.ConcurrentHashMap;

public class circuitBreaker {

    private static class CircuitState {
        int failureCount = 0;
        long lastFailureTime = 0;
        boolean isOpen = false;
    }

    private final ConcurrentHashMap<String, CircuitState> map = new ConcurrentHashMap<>();

    private static final int FAILURE_THRESHOLD = 3;
    private static final long OPEN_DURATION_MS = 5000; // 5 seconds

    public boolean allowRequest(String dependency) {
        map.putIfAbsent(dependency, new CircuitState());
        CircuitState state = map.get(dependency);

        if (state.isOpen) {
            long now = System.currentTimeMillis();

            // Check if cooldown passed → HALF-OPEN
            if (now - state.lastFailureTime > OPEN_DURATION_MS) {
                state.isOpen = false;
                state.failureCount = 0;
                return true; // allow trial request
            }

            return false; // still OPEN
        }

        return true; // CLOSED
    }

    public void recordSuccess(String dependency) {
        CircuitState state = map.get(dependency);
        if (state != null) {
            state.failureCount = 0;
            state.isOpen = false;
        }
    }

    public void recordFailure(String dependency) {
        map.putIfAbsent(dependency, new CircuitState());
        CircuitState state = map.get(dependency);

        state.failureCount++;
        state.lastFailureTime = System.currentTimeMillis();

        if (state.failureCount >= FAILURE_THRESHOLD) {
            state.isOpen = true;
        }
    }
}