package com.Dummy.demo.service.Simulation.depSimulation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.Dummy.demo.monitoring.SystemLoad.SystemLoadTracker;
import java.util.Random;
import com.Dummy.demo.service.Simulation.SimulationToggle;

@Component
public class crashSimulator {

    @Autowired
    private stateSimulator stateSimulator;

    @Autowired
    private SystemLoadTracker loadSimulator;
    @Autowired
    private SimulationToggle toggle;
    private Random random = new Random();

    public void checkAndCrash(String dependency) {
        if (!toggle.FailureisEnabled())
            return;
        stateSimulator.SystemState state = stateSimulator.getCurrentState();
        int load = loadSimulator.getCurrentLoad();

        double baseCrash = getBaseCrashRate(dependency);
        double stateFactor = getStateFactor(state);
        double loadFactor = getLoadFactor(load);

        // small randomness
        double noise = (random.nextDouble() * 0.02); // up to +2%

        double crashChance = baseCrash * stateFactor * loadFactor + noise;

        crashChance = Math.min(0.5, crashChance); // cap (avoid insanity)

        if (random.nextDouble() < crashChance) {
            throw new RuntimeException(dependency + " SERVICE DOWN");
        }
    }

    // -------- helpers --------

    private double getBaseCrashRate(String dep) {
        switch (dep) {
            case "DB":
                return 0.005;
            case "PAYMENT":
                return 0.02;
            case "THIRD_PARTY":
                return 0.015;
            default:
                return 0.01;
        }
    }

    private double getStateFactor(stateSimulator.SystemState state) {
        switch (state) {
            case NORMAL:
                return 1.0;
            case DEGRADED:
                return 2.0;
            case FAILING:
                return 4.0;
            default:
                return 1.0;
        }
    }

    private double getLoadFactor(int load) {
        if (load < 10)
            return 1.0;
        if (load < 50)
            return 1.5;
        return 2.5;
    }
}