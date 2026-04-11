package com.Dummy.demo.monitoring.model;

public class MetricsSnapshot {
	private double avgLatency;
	private double errorRate;
	private long p95Latency;
	private int requestCount;

	
	public MetricsSnapshot(double avgLatency, double errorRate, long p95Latency, int requestCount) {
        this.avgLatency = avgLatency;
        this.errorRate = errorRate;
        this.p95Latency = p95Latency;
        this.requestCount = requestCount;
    }
	
	@Override
    public String toString() {
        return "MetricsSnapshot{" +
                "avgLatency=" + avgLatency +
                ", errorRate=" + errorRate +
                ", p95Latency=" + p95Latency +
                ", requestCount=" + requestCount +
                '}';
    }
}
