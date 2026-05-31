package com.Dummy.demo.monitoring.trend.model;

public class MetricsHistoryEntry {
    private double avgLatency;
    private double errorRate;
    private double throughput;

    public MetricsHistoryEntry() {
    }

    public MetricsHistoryEntry(double avgLatency, double errorRate, double throughput) {
        this.avgLatency = avgLatency;
        this.errorRate = errorRate;
        this.throughput = throughput;
    }

    public double getAvgLatency() {
        return avgLatency;
    }

    public void setAvgLatency(double avgLatency) {
        this.avgLatency = avgLatency;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public double getThroughput() {
        return throughput;
    }

    public void setThroughput(double throughput) {
        this.throughput = throughput;
    }
}
