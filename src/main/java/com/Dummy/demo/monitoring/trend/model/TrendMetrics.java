package com.Dummy.demo.monitoring.trend.model;

public class TrendMetrics {
    private double latencySlope;
    private double errorSlope;
    private double throughputSlope;

    public TrendMetrics() {
    }

    public TrendMetrics(double latencySlope, double errorSlope, double throughputSlope) {
        this.latencySlope = latencySlope;
        this.errorSlope = errorSlope;
        this.throughputSlope = throughputSlope;
    }

    public double getLatencySlope() {
        return latencySlope;
    }

    public void setLatencySlope(double latencySlope) {
        this.latencySlope = latencySlope;
    }

    public double getErrorSlope() {
        return errorSlope;
    }

    public void setErrorSlope(double errorSlope) {
        this.errorSlope = errorSlope;
    }

    public double getThroughputSlope() {
        return throughputSlope;
    }

    public void setThroughputSlope(double throughputSlope) {
        this.throughputSlope = throughputSlope;
    }
}
