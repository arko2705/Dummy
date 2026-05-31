package com.Dummy.demo.service.externalDependency.client;

import com.Dummy.demo.service.externalDependency.model.externalRequest;
import com.Dummy.demo.service.externalDependency.model.externalResponse;
import com.Dummy.demo.service.externalDependency.resilience.rateLimiter;
import com.Dummy.demo.service.externalDependency.resilience.retryHandler;
import com.Dummy.demo.service.externalDependency.resilience.circuitBreaker;
import com.Dummy.demo.service.externalDependency.model.externalRequest;
import com.Dummy.demo.service.externalDependency.model.externalResponse;
import com.Dummy.demo.service.Simulation.depSimulation.crashSimulator;
import com.Dummy.demo.service.Simulation.depSimulation.errorSimulator;
import com.Dummy.demo.service.Simulation.depSimulation.DepLatencySimulator;
import com.Dummy.demo.service.externalDependency.simulationConfig;
import com.Dummy.demo.service.externalDependency.load.DependencyLoadTracker;
import com.Dummy.demo.monitoring.dependency.service.DependencyMetricsService;
import com.Dummy.demo.monitoring.error.service.errorLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class thirdPartyAPIClient {

    @Autowired
    private rateLimiter rateLimiter;

    @Autowired
    private retryHandler retryHandler;

    @Autowired
    private circuitBreaker circuitBreaker;

    @Autowired
    private DepLatencySimulator latencySimulator;

    @Autowired
    private errorSimulator errorSimulator;

    @Autowired
    private crashSimulator crashSimulator;
    @Autowired
    private DependencyLoadTracker loadTracker;
    @Autowired
    private DependencyMetricsService dependencyMetricsService;
    @Autowired
    private errorLogService errorLogService;

    private static final int TIMEOUT_THRESHOLD_MS = simulationConfig.THIRD_PARTY_TIMEOUT; // third-party is as slow as
                                                                                          // payment gateway

    public externalResponse callAPI(externalRequest request) {
        loadTracker.incAPI();

        try {
            return retryHandler.execute(() -> {

                String dep = "THIRD_PARTY";
                String depMetricName = "API";

                // 1️⃣ Circuit breaker
                if (!circuitBreaker.allowRequest(dep)) {
                    throw new RuntimeException("Circuit open for THIRD_PARTY");
                }

                // 2️⃣ Rate limit (this hits often)
                if (!rateLimiter.allowRequest(dep)) {
                    throw new RuntimeException("Rate limit exceeded for THIRD_PARTY");
                }

                long start = System.currentTimeMillis();
                boolean success = false;
                try {
                    // 3️⃣ Occasional crash (more than DB, less than payment)
                    crashSimulator.checkAndCrash(dep);

                    // 4️⃣ High latency
                    int latency = latencySimulator.applyLatency(dep);
                    try {
                        Thread.sleep(latency);
                    } catch (Exception e) {

                    }
                    // 5️⃣ Timeout (common)
                    if (latency > TIMEOUT_THRESHOLD_MS) {
                        throw new RuntimeException("Third-party timeout");
                    }

                    // 6️⃣ High failure rate
                    errorSimulator.checkAndThrow(dep);

                    boolean isPartial = Math.random() < 0.2;

                    Object data = isPartial ? "Empty Field" : "API_DATA";

                    String message = isPartial
                            ? "Partial response from third-party"
                            : "Third-party response success";
                    // 7️⃣ Success
                    circuitBreaker.recordSuccess(dep);
                    success = true;
                    return new externalResponse(
                            request.getRequestId(),
                            dep,
                            true,
                            message,
                            data);

                } catch (Exception e) {
                    circuitBreaker.recordFailure(dep);
                    throw e;
                } finally {
                    long end = System.currentTimeMillis();
                    dependencyMetricsService.recordDependencyCall(depMetricName, end - start, success);
                }
            });
        } catch (Exception e) {
            errorLogService.logDependencyFailure("API", e, errorLogService.extractOperation(request));
            throw e;
        } finally {
            loadTracker.decAPI();
        }
    }
}