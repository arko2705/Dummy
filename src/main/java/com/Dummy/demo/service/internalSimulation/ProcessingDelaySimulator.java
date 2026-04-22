package com.Dummy.demo.service.internalSimulation;

import java.util.Random;

public class ProcessingDelaySimulator implements FailureStrategy {

    private Random random = new Random();

    @Override
    public boolean shouldTrigger(Context ctx) {
        int diceRoll = random.nextInt(100);
        System.out.println("Processing delay dice roll: " + diceRoll + " for operation: " + ctx.operation);
        return diceRoll < 40; // 40% chance
    }

    @Override
    public void execute(Context ctx) {
        try {
            System.out.println("Processing delay for " + ctx.operation);
            Thread.sleep(2000); // 2 sec delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}