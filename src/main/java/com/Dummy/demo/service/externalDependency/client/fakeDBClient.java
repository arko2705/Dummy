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

import java.util.Random;

@Service
public class fakeDBClient {

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
    private final int TIMEOUT_THRESHOLD_MS = simulationConfig.DB_TIMEOUT;
    private Random random = new Random();

    public externalResponse fetchData(externalRequest request) {

        return retryHandler.execute(() -> {

            String dep = "DB";

            if (!circuitBreaker.allowRequest(dep)) {
                throw new RuntimeException("Circuit open for DB");
            }

            if (!rateLimiter.allowRequest(dep)) {
                throw new RuntimeException("Rate limit exceeded for DB");
            }

            try {
                // 1️⃣ Latency (DB is faster)
                int latency = latencySimulator.applyLatency(dep);

                if (latency > TIMEOUT_THRESHOLD_MS) {
                    throw new RuntimeException("DB timeout");
                }

                // 2️⃣ Occasional error (rare)
                errorSimulator.checkAndThrow(dep);

                // 3️⃣ Stale data simulation (10% chance)
                boolean isStale = random.nextDouble() < 0.1;

                Object data = isStale ? "OLD_DATA_VERSION" : "LATEST_DATA";

                circuitBreaker.recordSuccess(dep);

                return new externalResponse(
                        request.getRequestId(),
                        dep,
                        true,
                        isStale ? "Stale data returned" : "Fresh data",
                        data);

            } catch (Exception e) {
                circuitBreaker.recordFailure(dep);
                throw e;
            }
        });
    }
}