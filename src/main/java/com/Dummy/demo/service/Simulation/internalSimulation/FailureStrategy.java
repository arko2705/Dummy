package com.Dummy.demo.service.Simulation.internalSimulation;

public interface FailureStrategy {
    boolean shouldTrigger(Context ctx);

    void execute(Context ctx);
}