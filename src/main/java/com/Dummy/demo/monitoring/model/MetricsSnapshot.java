package com.Dummy.demo.monitoring.model;

public class MetricsSnapshot {
    private double avgLatency;
    private double errorRate;
    private long p95Latency;
    private int requestCount;
    private double throughput;

    public MetricsSnapshot(double avgLatency, double errorRate, long p95Latency, int requestCount, double throughput) {
        this.avgLatency = avgLatency;
        this.errorRate = errorRate;
        this.p95Latency = p95Latency;
        this.requestCount = requestCount;
        this.throughput = throughput;
    }

    @Override
    public String toString() {
        return "MetricsSnapshot{" +
                "avgLatency=" + avgLatency +
                ", errorRate=" + errorRate +
                ", p95Latency=" + p95Latency +
                ", requestCount=" + requestCount +
                ", throughput=" + throughput +
                '}';
    }
}
