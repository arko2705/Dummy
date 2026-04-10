package com.Dummy.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.Dummy.demo.service.HostMetricsService;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.Dummy.demo.model.hostMetricModel;

@RestController
@RequestMapping("/smash")
@Tag(name = "Metrics", description = "System metrics for monitoring and failure simulation")
public class metricsController {
    @GetMapping("/test")
    public String test() {
        return "App is running babbayyyyy";
    }

    // declare class as a object like Employee employee bro
    private HostMetricsService hostService;

    public metricsController(HostMetricsService hostService) {
        this.hostService = hostService;
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
