package com.Dummy.demo.service.externalDependency.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.Dummy.demo.service.Simulation.depSimulation.*;
import com.Dummy.demo.service.externalDependency.simulationConfig;
import com.Dummy.demo.service.externalDependency.model.*;
import com.Dummy.demo.service.externalDependency.resilience.rateLimiter;
import com.Dummy.demo.service.externalDependency.resilience.retryHandler;
import com.Dummy.demo.service.externalDependency.resilience.circuitBreaker;
import com.Dummy.demo.service.externalDependency.load.DependencyLoadTracker;

@Component
public class paymentGatewayClient {

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

    private static final int TIMEOUT_THRESHOLD_MS = simulationConfig.PAYMENT_TIMEOUT;

    public externalResponse processPayment(externalRequest request) {
        loadTracker.incPayment();
        try {
            return retryHandler.execute(() -> {

                String dep = "PAYMENT";

                if (!circuitBreaker.allowRequest(dep)) {
                    throw new RuntimeException("Circuit open");
                }

                if (!rateLimiter.allowRequest(dep)) {
                    throw new RuntimeException("Rate limit exceeded");
                }

                try {
                    crashSimulator.checkAndCrash(dep);

                    int latency = latencySimulator.applyLatency(dep);// baseLatency+loadBased latency-logic is
                                                                     // implemented
                                                                     // under latencySimulator
                    try {
                        Thread.sleep(latency);
                    } catch (Exception e) {

                    }
                    if (latency > TIMEOUT_THRESHOLD_MS) {
                        throw new RuntimeException("Timeout");
                    }

                    errorSimulator.checkAndThrow(dep);// random failure

                    circuitBreaker.recordSuccess(dep);

                    return new externalResponse(
                            request.getRequestId(),
                            dep,
                            true,
                            "Payment success",
                            null);// can add this later for realism

                } catch (Exception e) {
                    circuitBreaker.recordFailure(dep);
                    throw e;
                }
            });
        } finally {
            loadTracker.decPayment();
        }
    }
}