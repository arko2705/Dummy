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
        // register("CART_ADD", new StateInconsistencySimulator());
        // register("CART_ADD", new RandomCartFailure());
        register("PRODUCT_FETCH", new ProcessingDelaySimulator());// After this go check each of these methods,then come
                                                                  // back to internalErrorSimulator's inject() frm there
        register("PRODUCT_FETCH", new StateInconsistencySimulator());
        register("PRODUCT_FETCH", new SystemCrashSimulator());
        register("PRODUCT_UPDATE", new SystemCrashSimulator());
        register("PRODUCT_UPDATE", new StateInconsistencySimulator());
        register("PRODUCT_UPDATE", new ProcessingDelaySimulator());
        register("PRODUCT_ADD", new ProcessingDelaySimulator());// can add stateInconsistency in V2
        register("PRODUCT_ADD", new SystemCrashSimulator());
        register("PRODUCT_DELETE", new ProcessingDelaySimulator());
        register("PRODUCT_DELETE", new SystemCrashSimulator());

        register("CART_ADD", new StateInconsistencySimulator());
        register("CART_ADD", new ProcessingDelaySimulator());
        register("CART_ADD", new SystemCrashSimulator());
        register("CART_FETCH", new SystemCrashSimulator());
        register("CART_FETCH", new ProcessingDelaySimulator());
        register("CART_FETCH", new StateInconsistencySimulator());
        register("CART_DELETE", new ProcessingDelaySimulator());
        register("CART_DELETE", new SystemCrashSimulator());

        register("ORDER_CREATE", new StateInconsistencySimulator());
        register("ORDER_CREATE", new ProcessingDelaySimulator());
        register("ORDER_CREATE", new SystemCrashSimulator());
        register("ORDER_FETCH", new SystemCrashSimulator());
        register("ORDER_FETCH", new ProcessingDelaySimulator());
        register("ORDER_FETCH", new StateInconsistencySimulator());
        register("ORDER_DELETE", new ProcessingDelaySimulator());
        register("ORDER_DELETE", new SystemCrashSimulator());

        register("PAYMENT_PROCESS", new StateInconsistencySimulator());
        register("PAYMENT_PROCESS", new ProcessingDelaySimulator());
        register("PAYMENT_PROCESS", new SystemCrashSimulator());
        register("PAYMENT_FETCH", new SystemCrashSimulator());
        register("PAYMENT_FETCH", new StateInconsistencySimulator());
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
