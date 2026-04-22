package com.Dummy.demo.service.internalSimulation;

import java.util.Random;

public class SystemCrashSimulator implements FailureStrategy {

    private Random random = new Random();

    @Override
    public boolean shouldTrigger(Context ctx) {
        return random.nextInt(100) < 20; // 20% chance
    }

    @Override
    public void execute(Context ctx) {

        int type = random.nextInt(2);

        if (type == 0) {
            throw new RuntimeException("SYSTEM_DOWN");
        } else {
            throw new RuntimeException("OPERATION_FAILED: " + ctx.operation);
        }
    }
}
