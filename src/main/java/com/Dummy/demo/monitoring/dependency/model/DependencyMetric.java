package com.Dummy.demo.monitoring.dependency.model;

public class DependencyMetric {
    private double avgLatency;
    private double failureRate;
    private int requestCount;

    public DependencyMetric() {
    }

    public DependencyMetric(double avgLatency, double failureRate, int requestCount) {
        this.avgLatency = avgLatency;
        this.failureRate = failureRate;
        this.requestCount = requestCount;
    }

    public double getAvgLatency() {
        return avgLatency;
    }

    public void setAvgLatency(double avgLatency) {
        this.avgLatency = avgLatency;
    }

    public double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(double failureRate) {
        this.failureRate = failureRate;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }
}

