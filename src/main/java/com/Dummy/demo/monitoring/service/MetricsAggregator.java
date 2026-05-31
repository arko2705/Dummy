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
import com.Dummy.demo.monitoring.internal.model.InternalEvent;
import com.Dummy.demo.monitoring.internal.model.InternalEventType;
import com.Dummy.demo.monitoring.internal.model.InternalMetrics;
import com.Dummy.demo.monitoring.internal.service.InternalMetricsService;
import com.Dummy.demo.monitoring.model.EndpointMetric;
import com.Dummy.demo.monitoring.model.MetricsSnapshot;
import com.Dummy.demo.monitoring.model.RequestMetric;
import com.Dummy.demo.monitoring.reliability.model.ReliabilityMetrics;
import com.Dummy.demo.monitoring.trend.model.MetricsHistoryEntry;
import com.Dummy.demo.monitoring.trend.model.TrendMetrics;
import com.Dummy.demo.monitoring.trend.service.SnapshotHistoryService;

@Component
public class MetricsAggregator {

    private final RequestMetricsService metricsService;
    private final DependencyMetricsService dependencyMetricsService;
    private final InternalMetricsService internalMetricsService;
    private final SnapshotHistoryService snapshotHistoryService;

    private volatile MetricsSnapshot latestSnapshot;

    public MetricsAggregator(RequestMetricsService metricsService, DependencyMetricsService dependencyMetricsService,
            InternalMetricsService internalMetricsService, SnapshotHistoryService snapshotHistoryService) {
        this.metricsService = metricsService;
        this.dependencyMetricsService = dependencyMetricsService;
        this.internalMetricsService = internalMetricsService;
        this.snapshotHistoryService = snapshotHistoryService;
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
        InternalMetrics internalMetrics = aggregateInternalMetrics(
                internalMetricsService.getRecentEventsSnapshot());

        snapshotHistoryService.record(avgLatency, errorRate, throughput);
        TrendMetrics trendMetrics = calculateTrendMetrics(snapshotHistoryService.getHistorySnapshot());
        ReliabilityMetrics reliabilityMetrics = aggregateReliabilityMetrics();

        MetricsSnapshot snapshot = new MetricsSnapshot(
                avgLatency,
                errorRate,
                p95Latency,
                requestCount,
                throughput,
                endpointMetrics,
                dependencyMetrics,
                internalMetrics,
                trendMetrics,
                reliabilityMetrics);

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

    private InternalMetrics aggregateInternalMetrics(List<InternalEvent> events) {
        int processingDelayEvents = 0;
        int stateInconsistencyEvents = 0;
        int systemCrashEvents = 0;
        int concurrencyEvents = 0;

        for (InternalEvent event : events) {
            InternalEventType type = event.getEventType();
            if (type == InternalEventType.PROCESSING_DELAY) {
                processingDelayEvents++;
            } else if (type == InternalEventType.STATE_INCONSISTENCY) {
                stateInconsistencyEvents++;
            } else if (type == InternalEventType.SYSTEM_CRASH) {
                systemCrashEvents++;
            } else if (type == InternalEventType.CONCURRENCY_EVENT) {
                concurrencyEvents++;
            }
        }

        return new InternalMetrics(processingDelayEvents, stateInconsistencyEvents, systemCrashEvents,
                concurrencyEvents);
    }

    private TrendMetrics calculateTrendMetrics(List<MetricsHistoryEntry> history) {
        if (history.isEmpty()) {
            return new TrendMetrics(0.0, 0.0, 0.0);
        }

        int sampleCount = history.size();
        MetricsHistoryEntry first = history.get(0);
        MetricsHistoryEntry last = history.get(sampleCount - 1);

        double latencySlope = (last.getAvgLatency() - first.getAvgLatency()) / sampleCount;
        double errorSlope = (last.getErrorRate() - first.getErrorRate()) / sampleCount;
        double throughputSlope = (last.getThroughput() - first.getThroughput()) / sampleCount;

        return new TrendMetrics(latencySlope, errorSlope, throughputSlope);
    }

    private ReliabilityMetrics aggregateReliabilityMetrics() {
        int totalRequests = metricsService.getTotalRequests();
        int failedRequests = metricsService.getFailedRequests();

        double failureRate = totalRequests == 0 ? 0.0 : (double) failedRequests / totalRequests;

        List<Long> failureTimestamps = List.copyOf(metricsService.getFailureTimestamps());
        List<Long> downtimeStarts = List.copyOf(metricsService.getdownTimeStartTimes());
        List<Long> downtimeEnds = List.copyOf(metricsService.getdownTimeEndTimes());

        double mtbf = calculateMtbf(failureTimestamps);
        double mttr = calculateMttr(downtimeStarts, downtimeEnds);
        double availability = calculateAvailability(downtimeStarts, downtimeEnds);

        return new ReliabilityMetrics(failureRate, mtbf, mttr, availability);
    }

    private double calculateMtbf(List<Long> failureTimestamps) {
        if (failureTimestamps.size() < 2) {
            return 0.0;
        }

        long totalInterval = 0;
        for (int i = 1; i < failureTimestamps.size(); i++) {
            totalInterval += failureTimestamps.get(i) - failureTimestamps.get(i - 1);
        }

        int intervalCount = failureTimestamps.size() - 1;
        return (double) totalInterval / intervalCount;
    }

    private double calculateMttr(List<Long> downtimeStarts, List<Long> downtimeEnds) {
        int recoveryCount = Math.min(downtimeStarts.size(), downtimeEnds.size());
        if (recoveryCount == 0) {
            return 0.0;
        }

        long totalRecoveryDuration = 0;
        for (int i = 0; i < recoveryCount; i++) {
            totalRecoveryDuration += downtimeEnds.get(i) - downtimeStarts.get(i);
        }

        return (double) totalRecoveryDuration / recoveryCount;
    }

    private double calculateAvailability(List<Long> downtimeStarts, List<Long> downtimeEnds) {
        long now = System.currentTimeMillis();
        long totalTime = now - metricsService.getSystemsStartTime();
        if (totalTime <= 0) {
            return 1.0;
        }

        long totalDowntime = 0;
        int recoveryCount = Math.min(downtimeStarts.size(), downtimeEnds.size());
        for (int i = 0; i < recoveryCount; i++) {
            totalDowntime += downtimeEnds.get(i) - downtimeStarts.get(i);
        }

        if (downtimeStarts.size() > downtimeEnds.size()) {
            totalDowntime += now - downtimeStarts.get(downtimeStarts.size() - 1);
        }

        if (totalDowntime == 0) {
            return 1.0;
        }

        long uptime = totalTime - totalDowntime;
        if (uptime < 0) {
            uptime = 0;
        }

        return (double) uptime / (uptime + totalDowntime);
    }

    public MetricsSnapshot getLatestSnapshot() {
        return latestSnapshot;
    }
}
