package com.Dummy.demo.monitoring.depSimulation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class latencySimulator {

    @Autowired
    private stateSimulator stateSimulator;

    private Random random = new Random();

    public int applyLatency(String dependency) {

        stateSimulator.SystemState state = stateSimulator.getCurrentState();

        int baseLatency = getBaseLatency(dependency);
        int extraLatency = 0;

        switch (state) {
            case NORMAL:
                extraLatency = random.nextInt(50); // small jitter
                break;

            case DEGRADED:
                extraLatency = 100 + random.nextInt(300);
                break;

            case FAILING:
                extraLatency = 400 + random.nextInt(900);
                break;
        }

        int totalLatency = baseLatency + extraLatency;

        try {
            Thread.sleep(totalLatency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return totalLatency;
    }

    private int getBaseLatency(String dependency) {
        switch (dependency) {
            case "DB":
                return 50;
            case "PAYMENT":
                return 120;
            case "THIRD_PARTY":
                return 200;
            default:
                return 100;
        }
    }
}
