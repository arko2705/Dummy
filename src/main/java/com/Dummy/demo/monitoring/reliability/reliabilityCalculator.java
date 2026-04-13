package com.Dummy.demo.monitoring.reliability;

import com.Dummy.demo.monitoring.service.RequestMetricsService;

public class reliabilityCalculator {
    private RequestMetricsService metricsService;

    public reliabilityCalculator(RequestMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    private double getFailureRate() {
        int totalRequests = metricsService.getTotalRequests();
        int failedRequests = metricsService.getFailedRequests();
        if (totalRequests == 0) {
            return 0.0;
        }
        return (double) failedRequests / totalRequests;
    }

    private void crashRate() {
        // gotta make,its crashEvents/total time so far since system started.
    }

    private void MTBF() {
        // gotta make this total uptime / number of failures
    }

    private void MTTR() {
        // mean time to repair/recovery
    }

    private void availability() {
        // MTBF / (MTBF + MTTR)
    }

    private double probabilityOfFailureOnDemand() {
        // failureRate() and this are same, but failure rate is more used to system
        // health while probOfFailureOnDemand has applications in reliability
        return getFailureRate();
    }
}
