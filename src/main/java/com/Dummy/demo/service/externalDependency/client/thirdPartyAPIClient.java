package com.Dummy.demo.service.externalDependency.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.Dummy.demo.service.externalDependency.resilience.*;
import com.Dummy.demo.monitoring.simulation.*;
import com.Dummy.demo.service.externalDependency.model.*;


@Component
public class thirdPartyAPIClient {

    @Autowired
    private circuitBreaker circuitBreaker;

    @Autowired
    private retryHandler retryHandler;

    @Autowired
    private latencySimulator latencySimulator;

    @Autowired
    private errorSimulator errorSimulator;

    public externalResponse callAPI(externalRequest request) {

        return retryHandler.execute(() -> {

            if (!circuitBreaker.allowRequest("THIRD_PARTY")) {
                throw new RuntimeException("Circuit open for THIRD_PARTY");
            }

            try {
                latencySimulator.applyLatency("THIRD_PARTY"); 		//classes in simulation packages yet to be implemented
                errorSimulator.checkAndThrow("THIRD_PARTY");

                circuitBreaker.recordSuccess("THIRD_PARTY");

                return new externalResponse(
                        request.getRequestId(),
                        "THIRD_PARTY",
                        true,
                        "Third-party API success",
                        "External API Data"
                );

            } catch (Exception e) {
                circuitBreaker.recordFailure("THIRD_PARTY");
                throw e;
            }
        });
    }
}