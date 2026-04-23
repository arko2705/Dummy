package com.Dummy.demo.service.Simulation.internalSimulation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import com.Dummy.demo.service.Simulation.SimulationToggle;

@Service
public class InternalErrorSimulator {
    @Autowired
    private SimulationToggle toggle;
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
        register("PRODUCT_FETCH", new randomEndpointCrash());
        register("PRODUCT_UPDATE", new randomEndpointCrash());
        register("PRODUCT_UPDATE", new StateInconsistencySimulator());
        register("PRODUCT_UPDATE", new ProcessingDelaySimulator());
        register("PRODUCT_ADD", new ProcessingDelaySimulator());// can add stateInconsistency in V2
        register("PRODUCT_ADD", new randomEndpointCrash());
        register("PRODUCT_DELETE", new ProcessingDelaySimulator());
        register("PRODUCT_DELETE", new randomEndpointCrash());

        register("CART_ADD", new StateInconsistencySimulator());
        register("CART_ADD", new ProcessingDelaySimulator());
        register("CART_ADD", new randomEndpointCrash());
        register("CART_FETCH", new randomEndpointCrash());
        register("CART_FETCH", new ProcessingDelaySimulator());
        register("CART_FETCH", new StateInconsistencySimulator());
        register("CART_DELETE", new ProcessingDelaySimulator());
        register("CART_DELETE", new randomEndpointCrash());

        register("ORDER_CREATE", new StateInconsistencySimulator());
        register("ORDER_CREATE", new ProcessingDelaySimulator());
        register("ORDER_CREATE", new randomEndpointCrash());
        register("ORDER_FETCH", new randomEndpointCrash());
        register("ORDER_FETCH", new ProcessingDelaySimulator());
        register("ORDER_FETCH", new StateInconsistencySimulator());
        register("ORDER_DELETE", new ProcessingDelaySimulator());
        register("ORDER_DELETE", new randomEndpointCrash());

        register("PAYMENT_PROCESS", new StateInconsistencySimulator());
        register("PAYMENT_PROCESS", new ProcessingDelaySimulator());
        register("PAYMENT_PROCESS", new randomEndpointCrash());
        register("PAYMENT_FETCH", new randomEndpointCrash());
        register("PAYMENT_FETCH", new StateInconsistencySimulator());
        register("PAYMENT_FETCH", new ProcessingDelaySimulator());

    }

    public void inject(String operation, Context ctx) {
        if (!toggle.FailureisEnabled())// when it returns false,we do not loop over the strategies
            return;
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
