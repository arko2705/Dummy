package com.Dummy.demo.service.SystemState;

import org.springframework.stereotype.Component;

import java.util.Random;

import com.Dummy.demo.monitoring.service.RequestMetricsService;

//I(arko) need to understand this entirely,have an idea 
@Component
public class SystemCrashEngine {

    private final RequestMetricsService metricsService;
    private final Random random = new Random();

    private boolean systemDown = false;
    private long recoveryTime = 0;

    public SystemCrashEngine(RequestMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    // called before processing request OR inside interceptor
    public void evaluateSystemHealth(int currentLoad) {

        long now = System.currentTimeMillis();

        // ✅ if system is already down → check recovery
        if (systemDown) {
            if (now >= recoveryTime) {
                systemDown = false;
                metricsService.markSystemUp();// marks the uptime
            }
            return;
        }

        // 🔥 failure pressure factors (from your existing metrics)
        int failures = metricsService.getFailedRequests();
        int pastCrashes = metricsService.getdownTimeStartTimes().size();

        // 🎲 probabilistic crash trigger
        double crashScore = (currentLoad * 0.4) +
                (failures * 0.3) +
                (pastCrashes * 2) +
                (random.nextInt(10));

        if (crashScore > 25) {

            systemDown = true;
            metricsService.markSystemDown();

            // ⏱ non-linear downtime (important)
            long downtime = (long) (2000 +
                    Math.pow(currentLoad + 1, 1.5) * 30 +
                    failures * 200 +
                    random.nextInt(2000));

            recoveryTime = now + downtime;
        }
    }

    public boolean isSystemDown() {
        return systemDown;
    }
}