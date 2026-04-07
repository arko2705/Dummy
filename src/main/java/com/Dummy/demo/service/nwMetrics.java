package com.Dummy.demo.service;

import java.util.Random;
import java.lang.Thread;

public class nwMetrics {
    public int nwLatency;
    Random rand = new Random();

    public int nwDelayCalc() throws InterruptedException {
        nwLatency = rand.nextInt(400);
        Thread.sleep(nwLatency);
        return nwLatency;
    }
}
