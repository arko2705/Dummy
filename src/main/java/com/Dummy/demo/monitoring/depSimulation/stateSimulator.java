package com.Dummy.demo.monitoring.depSimulation;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

@Component
public class stateSimulator {

    public enum SystemState {
        NORMAL,
        DEGRADED,
        FAILING
    }

    @Autowired
    private loadSimulator loadSimulator;

    public SystemState getCurrentState() {

        double noise = Math.random(); // 0–1
        int load = loadSimulator.getCurrentLoad();

        if (load < 10) {
            // Mostly normal, rare spikes
            if (noise < 0.05)
                return SystemState.DEGRADED;
            return SystemState.NORMAL;
        }

        if (load < 50) {
            // Mixed behavior
            if (noise < 0.2)
                return SystemState.FAILING;
            if (noise < 0.6)
                return SystemState.DEGRADED;
            return SystemState.NORMAL;
        }

        // High load → mostly failing but unstable
        if (noise < 0.7)
            return SystemState.FAILING;
        if (noise < 0.9)
            return SystemState.DEGRADED;
        return SystemState.NORMAL;
    }
}
