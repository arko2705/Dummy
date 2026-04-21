package com.Dummy.demo.service.internalSimulation;

import java.util.Random;

public class RandomCartFailure implements FailureStrategy {

    private Random random = new Random();

    @Override
    public boolean shouldTrigger(Context ctx) {
        return ctx.operation.equals("CART_ADD") && random.nextInt(100) < 30;
        // 30% chance to fail
    }

    @Override
    public void execute(Context ctx) {
        throw new RuntimeException("Simulated cart failure");
    }
}
