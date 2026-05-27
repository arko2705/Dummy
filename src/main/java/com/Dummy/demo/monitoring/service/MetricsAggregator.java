package com.Dummy.demo.monitoring.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.Dummy.demo.monitoring.dependency.model.DependencyEvent;
import com.Dummy.demo.monitoring.dependency.model.DependencyMetric;
import com.Dummy.demo.monitoring.dependency.service.DependencyMetricsService;
import com.Dummy.demo.monitoring.model.EndpointMetric;
import com.Dummy.demo.monitoring.model.MetricsSnapshot;
import com.Dummy.demo.monitoring.model.RequestMetric;

@Component
public class MetricsAggregator {

    private final RequestMetricsService metricsService;
    private final DependencyMetricsService dependencyMetricsService;

    private volatile MetricsSnapshot latestSnapshot;

    public MetricsAggregator(RequestMetricsService metricsService, DependencyMetricsService dependencyMetricsService) {
        this.metricsService = metricsService;
        this.dependencyMetricsService = dependencyMetricsService;
    }

    @Scheduled(fixedRate = 5000)
    public void aggregateMetrics() {

        double avgLatency = metricsService.getAverageLatency();
        double errorRate = metricsService.getErrorRate();
        long p95Latency = metricsService.getP95Latency();
        int requestCount = metricsService.getRequestCount();
        double throughput = metricsService.getThroughput();
        Map<String, EndpointMetric> endpointMetrics = aggregateEndpointMetrics(
                metricsService.getRecentRequestsSnapshot());
        Map<String, DependencyMetric> dependencyMetrics = aggregateDependencyMetrics(
                dependencyMetricsService.getRecentEventsSnapshot());

        MetricsSnapshot snapshot = new MetricsSnapshot(
                avgLatency,
                errorRate,
                p95Latency,
                requestCount,
                throughput,
                endpointMetrics,
                dependencyMetrics);

        latestSnapshot = snapshot;
    }

    private Map<String, EndpointMetric> aggregateEndpointMetrics(List<RequestMetric> requests) {
        if (requests.isEmpty()) {
            return Map.of();
        }

        Map<String, List<RequestMetric>> byEndpoint = requests.stream()
                .collect(Collectors.groupingBy(RequestMetric::getEndpoint));

        Map<String, EndpointMetric> endpointMetrics = new HashMap<>();
        for (Map.Entry<String, List<RequestMetric>> entry : byEndpoint.entrySet()) {
            List<RequestMetric> endpointRequests = entry.getValue();
            int count = endpointRequests.size();
            double endpointAvgLatency = endpointRequests.stream()
                    .mapToLong(RequestMetric::getLatency)
                    .average()
                    .orElse(0.0);
            long endpointErrorCount = endpointRequests.stream()
                    .filter(RequestMetric::isError)
                    .count();
            double endpointErrorRate = count == 0 ? 0.0 : (double) endpointErrorCount / count;

            endpointMetrics.put(entry.getKey(),
                    new EndpointMetric(endpointAvgLatency, endpointErrorRate, count));
        }
        return endpointMetrics;
    }

    private Map<String, DependencyMetric> aggregateDependencyMetrics(List<DependencyEvent> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        Map<String, List<DependencyEvent>> byDependency = events.stream()
                .collect(Collectors.groupingBy(DependencyEvent::getDependencyName));

        Map<String, DependencyMetric> dependencyMetrics = new HashMap<>();
        for (Map.Entry<String, List<DependencyEvent>> entry : byDependency.entrySet()) {
            List<DependencyEvent> depEvents = entry.getValue();
            int count = depEvents.size();
            double avgLatency = depEvents.stream()
                    .mapToLong(DependencyEvent::getLatency)
                    .average()
                    .orElse(0.0);
            long failureCount = depEvents.stream()
                    .filter(e -> !e.isSuccess())
                    .count();
            double failureRate = count == 0 ? 0.0 : (double) failureCount / count;

            dependencyMetrics.put(entry.getKey(), new DependencyMetric(avgLatency, failureRate, count));
        }
        return dependencyMetrics;
    }

    public MetricsSnapshot getLatestSnapshot() {
        return latestSnapshot;
    }
}
