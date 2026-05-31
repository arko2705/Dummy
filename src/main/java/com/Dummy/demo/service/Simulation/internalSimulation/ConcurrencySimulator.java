package com.Dummy.demo.service.Simulation.internalSimulation;

// Stub for v2; implements FailureStrategy so activation can be recorded when registered.
public class ConcurrencySimulator implements FailureStrategy {

    @Override
    public boolean shouldTrigger(Context ctx) {
        return false;
    }

    @Override
    public void execute(Context ctx) {
    }
}
