package com.Dummy.demo.service.Simulation.internalSimulation;

import java.util.Random;

public class randomEndpointCrash implements FailureStrategy {

    private Random random = new Random();

    @Override
    public boolean shouldTrigger(Context ctx) {
        return random.nextInt(100) < 20; // 20% chance
    }

    @Override
    public void execute(Context ctx) {

        int type = random.nextInt(2);

        if (type == 0) {
            throw new RuntimeException("OPERATION_FAILED: " + ctx.operation + " simulated crash");
        }
    }
}
