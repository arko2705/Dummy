package com.Dummy.demo.monitoring.reliability;

import com.Dummy.demo.monitoring.service.RequestMetricsService;

public class reliabilityCalculator {
    private RequestMetricsService metricsService;

    public reliabilityCalculator(RequestMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    public double getFailureRate() {
        int totalRequests = metricsService.getTotalRequests();
        int failedRequests = metricsService.getFailedRequests();
        if (totalRequests == 0) {
            return 0.0;
        }
        return (double) failedRequests / totalRequests;
    }

    public double crashRate() {
        // gotta make,its crashEvents/total time so far since system started.
        long totalTime = System.currentTimeMillis() - metricsService.getSystemsStartTime();

        int crashes = Math.min(
                metricsService.getdownTimeStartTimes().size(),
                metricsService.getdownTimeEndTimes().size());

        if (totalTime == 0)
            return 0;

        return (double) crashes / totalTime;
    }

    public double getMTBF() {
        long totalTime = System.currentTimeMillis() - metricsService.getSystemsStartTime();
        int failures = metricsService.getFailureTimestamps().size();

        if (failures == 0) {
            return totalTime; // no failures → very high MTBF
        }

        return (double) totalTime / failures;
    }

    public double getMTTR() {
        // gotta understand this myself,will understand all this during testing me and
        // animesh
        int size = Math.min(
                metricsService.getdownTimeStartTimes().size(),
                metricsService.getdownTimeEndTimes().size());

        if (size == 0)
            return 0;

        long totalDowntime = 0;

        for (int i = 0; i < size; i++) {
            totalDowntime += (metricsService.getdownTimeEndTimes().get(i)
                    - metricsService.getdownTimeStartTimes().get(i));
        }

        return (double) totalDowntime / size;
    }

    public double getAvailability() {
        long totalTime = System.currentTimeMillis() - metricsService.getSystemsStartTime();
        if (totalTime == 0) {
            return 1.0;
        }
        long totalDowntime = 0;
        int size = Math.min(
                metricsService.getdownTimeStartTimes().size(),
                metricsService.getdownTimeEndTimes().size());

        for (int i = 0; i < size; i++) {
            totalDowntime += (metricsService.getdownTimeEndTimes().get(i)
                    - metricsService.getdownTimeStartTimes().get(i));
        }
        long uptime = totalTime - totalDowntime;
        double availability = (double) uptime / totalTime;
        return availability;
    }

    private double probabilityOfFailureOnDemand() {
        // failureRate() and this are same, but failure rate is more used to system
        // health while probOfFailureOnDemand has applications in reliability
        return getFailureRate();
    }
}
