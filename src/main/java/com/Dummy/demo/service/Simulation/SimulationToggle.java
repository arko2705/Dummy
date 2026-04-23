package com.Dummy.demo.service.Simulation;

import org.springframework.stereotype.Component;

@Component
public class SimulationToggle {

    private volatile boolean FailureEnabled = true;

    public boolean FailureisEnabled() {
        return FailureEnabled;
    }

    public void enable() {
        FailureEnabled = true;
    }

    public void disable() {
        FailureEnabled = false;
    }
}
