package com.Dummy.demo.service.internalSimulation;

public interface FailureStrategy {
    boolean shouldTrigger(Context ctx);

    void execute(Context ctx);
}