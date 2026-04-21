package com.Dummy.demo.monitoring.depSimulation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.Dummy.demo.monitoring.depSimulation.stateSimulator.SystemState;

import java.time.LocalTime;
import java.util.Random;

@Component
public class errorSimulator {

    @Autowired
    private stateSimulator stateSimulator;
    private Random random = new Random();

    public void checkAndThrow(String dependency) {

        stateSimulator.SystemState state = stateSimulator.getCurrentState();

        double base = getBaseFailure(dependency);
        double stateFactor = getStateMultiplier(state);
        double timeFactor = getTimeOfDayFactor();
        double correlationFactor = getCorrelationFactor(dependency, state);

        // 🔥 noise (±10%)
        double noise = (random.nextDouble() * 0.2) - 0.1;

        // 🔥 rare spike (5% chance)
        double spike = (random.nextDouble() < 0.05) ? 0.3 : 0.0;

        double failureRate = base * stateFactor * timeFactor * correlationFactor
                + noise + spike;

        // clamp
        failureRate = Math.max(0, Math.min(0.95, failureRate));

        if (random.nextDouble() < failureRate) {
            throw new RuntimeException(dependency + " failed");
        }
    }

    // ---------------- helpers ----------------

    private double getBaseFailure(String dep) {
        switch (dep) {
            case "DB":
                return 0.05;
            case "PAYMENT":
                return 0.15;
            case "THIRD_PARTY":
                return 0.25;
            default:
                return 0.1;
        }
    }

    private double getStateMultiplier(stateSimulator.SystemState state) {
        switch (state) {
            case NORMAL:
                return 1.0;
            case DEGRADED:
                return 2.0;
            case FAILING:
                return 3.5;
            default:
                return 1.0;
        }
    }

    // 🔥 Time-based variation (light influence)
    private double getTimeOfDayFactor() {
        int hour = LocalTime.now().getHour();

        if (hour >= 9 && hour <= 17) {
            return 1.2; // peak hours
        } else {
            return 0.9; // off-peak
        }
    }

    // 🔥 Light correlation (not too strong)
    private double getCorrelationFactor(String dependency, stateSimulator.SystemState state) {

        // If system is failing → everything slightly worse
        if (state == SystemState.FAILING) {
            if (dependency.equals("PAYMENT"))
                return 1.3;
            if (dependency.equals("THIRD_PARTY"))
                return 1.2;
        }

        return 1.0;
    }
}
