package com.Dummy.demo.service.externalDependency.load;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class DependencyLoadTracker {

    private AtomicInteger dbLoad = new AtomicInteger(0);
    private AtomicInteger paymentLoad = new AtomicInteger(0);
    private AtomicInteger apiLoad = new AtomicInteger(0);

    // DB
    public void incDB() {
        dbLoad.incrementAndGet();
    }

    public void decDB() {
        dbLoad.decrementAndGet();
    }

    public int getDBLoad() {
        return dbLoad.get();
    }

    // Payment
    public void incPayment() {
        paymentLoad.incrementAndGet();
    }

    public void decPayment() {
        paymentLoad.decrementAndGet();
    }

    public int getPaymentLoad() {
        return paymentLoad.get();
    }

    // Third-party API
    public void incAPI() {
        apiLoad.incrementAndGet();
    }

    public void decAPI() {
        apiLoad.decrementAndGet();
    }

    public int getAPILoad() {
        return apiLoad.get();
    }
}