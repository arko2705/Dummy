package com.Dummy.demo.service.externalDependency.client;

import com.Dummy.demo.service.externalDependency.model.externalRequest;
import com.Dummy.demo.service.externalDependency.model.externalResponse;
import com.Dummy.demo.service.externalDependency.resilience.rateLimiter;
import com.Dummy.demo.service.externalDependency.resilience.retryHandler;
import com.Dummy.demo.service.externalDependency.resilience.circuitBreaker;
import com.Dummy.demo.service.externalDependency.model.externalRequest;
import com.Dummy.demo.service.externalDependency.model.externalResponse;
import com.Dummy.demo.monitoring.depSimulation.crashSimulator;
import com.Dummy.demo.monitoring.depSimulation.errorSimulator;
import com.Dummy.demo.monitoring.depSimulation.latencySimulator;
import com.Dummy.demo.service.externalDependency.simulationConfig;

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
    private latencySimulator latencySimulator;

    @Autowired
    private errorSimulator errorSimulator;

    @Autowired
    private crashSimulator crashSimulator;

    private static final int TIMEOUT_THRESHOLD_MS = simulationConfig.THIRD_PARTY_TIMEOUT; // third-party is as slow as
                                                                                          // payment gateway

    public externalResponse callAPI(externalRequest request) {

        return retryHandler.execute(() -> {

            String dep = "THIRD_PARTY";

            // 1️⃣ Circuit breaker
            if (!circuitBreaker.allowRequest(dep)) {
                throw new RuntimeException("Circuit open for THIRD_PARTY");
            }

            // 2️⃣ Rate limit (this hits often)
            if (!rateLimiter.allowRequest(dep)) {
                throw new RuntimeException("Rate limit exceeded for THIRD_PARTY");
            }

            try {
                // 3️⃣ Occasional crash (more than DB, less than payment)
                crashSimulator.checkAndCrash(dep);

                // 4️⃣ High latency
                int latency = latencySimulator.applyLatency(dep);

                // 5️⃣ Timeout (common)
                if (latency > TIMEOUT_THRESHOLD_MS) {
                    throw new RuntimeException("Third-party timeout");
                }

                // 6️⃣ High failure rate
                errorSimulator.checkAndThrow(dep);

                boolean isPartial = Math.random() < 0.2;

                Object data = isPartial ? null : "API_DATA";

                String message = isPartial
                        ? "Partial response from third-party"
                        : "Third-party response success";
                // 7️⃣ Success
                circuitBreaker.recordSuccess(dep);
                return new externalResponse(
                        request.getRequestId(),
                        dep,
                        true,
                        "Third-party response success",
                        "API_DATA");

            } catch (Exception e) {
                circuitBreaker.recordFailure(dep);
                throw e;
            }
        });
    }
}