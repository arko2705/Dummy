package com.Dummy.demo.monitoring.SLO;

import com.Dummy.demo.monitoring.service.RequestMetricsService;
import com.Dummy.demo.monitoring.model.MetricsSnapshot;
import com.Dummy.demo.monitoring.reliability.reliabilityCalculator;

public class SLOEvaluator {
    SLOConfig config;
    RequestMetricsService metricsService;
    reliabilityCalculator reliabilityCalculator;

    public SLOEvaluator(SLOConfig config, RequestMetricsService metricsService,
            reliabilityCalculator reliabilityCalculator) {
        this.config = config;
        this.metricsService = metricsService;
        this.reliabilityCalculator = reliabilityCalculator;
    }

    public boolean isSLOViolated() {
        return config.getMaxErrorRate() < metricsService.getErrorRate()
                || config.getMinAvailability() < reliabilityCalculator.getAvailability()
                || config.getP95LatencyThresholdMs() < metricsService.getP95Latency()
                || config.getMaxAvgLatencyMs() < metricsService.getAverageLatency();

    }

    public boolean isLatencyViolated(long latency) {
        return latency > config.getMaxLatencyMs();
    }

    public boolean isErrorRateViolated(double errorRate) {
        return errorRate > config.getMaxErrorRate();
    }

    public boolean isAvailabilityViolated(double availability) {
        return availability < config.getMinAvailability();
    }

    public boolean isP95LatencyViolated(long p95Latency) {
        return p95Latency > config.getP95LatencyThresholdMs();
    }

    public boolean isAvgLatencyViolated(long avgLatency) {
        return avgLatency > config.getMaxAvgLatencyMs();
    }
}
