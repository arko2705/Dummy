package com.Dummy.demo.service.internalSimulation;

import java.util.Random;

public class ProcessingDelaySimulator implements FailureStrategy {

    private Random random = new Random();

    @Override
    public boolean shouldTrigger(Context ctx) {
        return random.nextInt(100) < 40; // 40% chance
    }

    @Override
    public void execute(Context ctx) {
        try {
            Thread.sleep(2000); // 2 sec delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}