package com.Dummy.demo.monitoring.model;

public class EndpointMetric {
    private double avgLatency;
    private double errorRate;
    private int requestCount;

    public EndpointMetric() {
    }

    public EndpointMetric(double avgLatency, double errorRate, int requestCount) {
        this.avgLatency = avgLatency;
        this.errorRate = errorRate;
        this.requestCount = requestCount;
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

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }
}
