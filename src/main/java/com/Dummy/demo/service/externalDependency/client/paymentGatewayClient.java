package com.Dummy.demo.service.externalDependency.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.Dummy.demo.service.externalDependency.resilience.*;
import com.Dummy.demo.monitoring.simulation.*;
import com.Dummy.demo.service.externalDependency.model.*;

@Component
public class paymentGatewayClient {

    @Autowired
    private circuitBreaker circuitBreaker;

    @Autowired
    private retryHandler retryHandler;

    @Autowired
    private latencySimulator latencySimulator;

    @Autowired
    private errorSimulator errorSimulator;

    public externalResponse processPayment(externalRequest request) {

        return retryHandler.execute(() -> {

            if (!circuitBreaker.allowRequest("PAYMENT")) {
                throw new RuntimeException("Circuit open for PAYMENT");
            }

            try {
                latencySimulator.applyLatency("PAYMENT"); 	//classes in simulation packages yet to be implemented
                errorSimulator.checkAndThrow("PAYMENT");

                circuitBreaker.recordSuccess("PAYMENT");

                return new externalResponse(
                        request.getRequestId(),
                        "PAYMENT",
                        true,
                        "Payment successful",
                        null
                );

            } catch (Exception e) {
                circuitBreaker.recordFailure("PAYMENT");
                throw e;
            }
        });
    }
}