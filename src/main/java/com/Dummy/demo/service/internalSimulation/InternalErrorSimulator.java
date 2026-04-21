package com.Dummy.demo.service.internalSimulation;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class InternalErrorSimulator {

    private final Map<String, List<FailureStrategy>> registry = new HashMap<>();

    public void register(String key, FailureStrategy strategy) {
        registry.computeIfAbsent(key, k -> new ArrayList<>()).add(strategy);// check notes 4
    }

    public InternalErrorSimulator() {
        register("CART_ADD", new RandomCartFailure());
        register("PRODUCT_FETCH", new ProcessingDelaySimulator());
        register("PRODUCT_ADD", new ProcessingDelaySimulator());
        register("PRODUCT_UPDATE", new ProcessingDelaySimulator());
        register("PRODUCT_DELETE", new ProcessingDelaySimulator());

        register("CART_ADD", new ProcessingDelaySimulator());
        register("CART_DELETE", new ProcessingDelaySimulator());
        register("CART_FETCH", new ProcessingDelaySimulator());

        register("ORDER_CREATE", new ProcessingDelaySimulator());
        register("ORDER_FETCH", new ProcessingDelaySimulator());
        register("ORDER_DELETE", new ProcessingDelaySimulator());

        register("PAYMENT_PROCESS", new ProcessingDelaySimulator());
        register("PAYMENT_FETCH", new ProcessingDelaySimulator());
    }

    public void inject(String operation, Context ctx) {
        List<FailureStrategy> strategies = registry.get(operation);
        if (strategies == null)
            return;

        for (FailureStrategy s : strategies) {
            if (s.shouldTrigger(ctx)) {
                s.execute(ctx);
            }
        }
    }
}
