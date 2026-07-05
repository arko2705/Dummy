package com.Dummy.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.Dummy.demo.service.HostMetricsService;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.Dummy.demo.model.hostMetricModel;
import com.Dummy.demo.monitoring.model.MetricsSnapshot;
import com.Dummy.demo.monitoring.service.MetricsAggregator;
import com.Dummy.demo.monitoring.service.RequestMetricsService;

@RestController
@RequestMapping("/api")
@Tag(name = "Metrics", description = "System metrics for monitoring and failure simulation")
public class metricsController {
    @GetMapping("/test")
    public String test() {
        return "App is running babbayyyyy";
    }

    // declare class as a object like Employee employee bro
    private HostMetricsService hostService;
    @Autowired
    private MetricsAggregator aggregator;
    @Autowired
    private RequestMetricsService metricsService;

    public metricsController(HostMetricsService hostService) { // Something i entirely missed,but basically Spring
                                                               // creates bean components labelled with "@" at startup,
                                                               // and injects them after wherever necessary. So the
                                                               // hostService is first created by spring,and then
                                                               // injected here to eequal local hostSerice to original.
        this.hostService = hostService;
    }

    @GetMapping("/metrics")
    public MetricsSnapshot getMetrics() {
        MetricsSnapshot snapshot = aggregator.getLatestSnapshot();
        if (snapshot == null) {
            // First scheduled aggregation has not run yet (fixedRate = 5s after startup)
            return new MetricsSnapshot(0, 0, 0, 0, 0);
        }
        return snapshot;
    }

    @GetMapping("/metrics/details")
    public Map<String, Object> getDetailedMetrics() {

        Map<String, Object> data = new HashMap<>();

        data.put("totalRequests", metricsService.getTotalRequests());
        data.put("failedRequests", metricsService.getFailedRequests());
        data.put("errorRate", metricsService.getErrorRate());
        data.put("avgLatency", metricsService.getAverageLatency());
        data.put("p95Latency", metricsService.getP95Latency());
        data.put("throughput", metricsService.getThroughput());
        data.put("failureTimestamps", metricsService.getFailureTimestamps());
        data.put("downtimeStart", metricsService.getdownTimeStartTimes());
        data.put("downtimeEnd", metricsService.getdownTimeEndTimes());

        return data;
    }

    


    @GetMapping("/hostMetrics")
    public hostMetricModel getHostMetrics() {
        return hostService.gethostMetrics();
    }

    @GetMapping("/networkMetrics")
    public int getnwmetrics() {
        return 10;
    }

    @GetMapping("/appMetrics")
    public int getappetrics() {
        return 10;
    }

    @GetMapping("/externalDependencyMetrics")
    public int getEDmetrics() {
        return 10;
    }
}
