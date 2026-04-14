package com.Dummy.demo.monitoring.SLO;

public class SLOConfig {
    public static final long MAX_LATENCY_MS = 200; // Currently a single request latency is an unuseful metric,unless
                                                   // extremem spike. WIll determine later.
    public static final double MAX_ERROR_RATE = 0.01;
    public static final double MIN_AVAILABILITY = 0.99;
    public static final long P95_LATENCY_THRESHOLD_MS = 300;
    public static final long MAX_AVG_LATENCY_MS = 150;

    public long getMaxLatencyMs() {
        return MAX_LATENCY_MS;
    }

    public double getMaxErrorRate() {
        return MAX_ERROR_RATE;
    }

    public double getMinAvailability() {
        return MIN_AVAILABILITY;
    }

    public long getP95LatencyThresholdMs() {
        return P95_LATENCY_THRESHOLD_MS;
    }

    public long getMaxAvgLatencyMs() {
        return MAX_AVG_LATENCY_MS;
    }

}
