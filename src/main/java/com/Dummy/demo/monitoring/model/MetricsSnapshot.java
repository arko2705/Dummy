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

    public long getP95Latency() {
        return p95Latency;
    }

    public void setP95Latency(long p95Latency) {
        this.p95Latency = p95Latency;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public double getThroughput() {
        return throughput;
    }

    public void setThroughput(double throughput) {
        this.throughput = throughput;
    }
}
