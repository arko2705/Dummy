package com.Dummy.demo.monitoring.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.Dummy.demo.monitoring.model.MetricsSnapshot;

@Component
public class MetricsAggregator {

    private final RequestMetricsService metricsService;

    private volatile MetricsSnapshot latestSnapshot; // store latest snapshot

    public MetricsAggregator(RequestMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Scheduled(fixedRate = 5000) // Runs every 5000ms basically every 5s
    public void aggregateMetrics() {

        double avgLatency = metricsService.getAverageLatency();
        double errorRate = metricsService.getErrorRate();
        long p95Latency = metricsService.getP95Latency();
        int requestCount = metricsService.getRequestCount();
        double throughput = metricsService.getThroughput();
        MetricsSnapshot snapshot = new MetricsSnapshot(
                avgLatency,
                errorRate,
                p95Latency,
                requestCount,
                throughput);
        
        latestSnapshot = snapshot;

        // For testing I'm just logging it for now. Will have to think of a way to send
        // it to the ML system
        //System.out.println(snapshot);

    }
    public MetricsSnapshot getLatestSnapshot() {
        return latestSnapshot;
    }
}
