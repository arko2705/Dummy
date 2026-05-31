package com.Dummy.demo.service.Simulation.internalSimulation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import com.Dummy.demo.monitoring.error.model.ErrorCategory;
import com.Dummy.demo.monitoring.error.service.errorLogService;
import com.Dummy.demo.monitoring.internal.model.InternalEventType;
import com.Dummy.demo.monitoring.internal.service.InternalMetricsService;
import com.Dummy.demo.service.Simulation.SimulationToggle;

@Service
public class InternalErrorSimulator {
    @Autowired
    private SimulationToggle toggle;
    @Autowired
    private errorLogService errorLogService;
    @Autowired
    private InternalMetricsService internalMetricsService;
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
                recordInternalEvent(s, ctx);
                logSimulationEvent(s, ctx);
            }
        }
    }

    private void recordInternalEvent(FailureStrategy strategy, Context ctx) {
        InternalEventType eventType = mapStrategyToEventType(strategy);
        if (eventType != null) {
            internalMetricsService.recordEvent(eventType, ctx.operation, ctx.service);
        }
    }

    private InternalEventType mapStrategyToEventType(FailureStrategy strategy) {
        if (strategy instanceof ProcessingDelaySimulator) {
            return InternalEventType.PROCESSING_DELAY;
        }
        if (strategy instanceof StateInconsistencySimulator) {
            return InternalEventType.STATE_INCONSISTENCY;
        }
        if (strategy instanceof ConcurrencySimulator) {
            return InternalEventType.CONCURRENCY_EVENT;
        }
        return null;
    }

    private void logSimulationEvent(FailureStrategy strategy, Context ctx) {
        if (strategy instanceof StateInconsistencySimulator) {
            String message = describeStateInconsistency(ctx);
            if (message != null) {
                errorLogService.logStructured(ErrorCategory.STATE_INCONSISTENCY, message, ctx.service,
                        ctx.operation, null);
            }
        } else if (strategy instanceof ProcessingDelaySimulator) {
            errorLogService.logStructured(ErrorCategory.INTERNAL_FAILURE,
                    "Processing delay simulated for " + ctx.operation, ctx.service, ctx.operation, null);
        }
    }

    private String describeStateInconsistency(Context ctx) {
        if (ctx.data == null || ctx.data.isEmpty()) {
            return null;
        }
        if (ctx.data.containsKey("duplicateOrders")) {
            return "Duplicate orders simulated";
        }
        if (ctx.data.containsKey("missingOrders")) {
            return "Missing orders simulated";
        }
        if (ctx.data.containsKey("wrongOrderStatus")) {
            return "Wrong order status simulated";
        }
        if (ctx.data.containsKey("emptyOrder")) {
            return "Empty order simulated";
        }
        if (ctx.data.containsKey("duplicateProducts")) {
            return "Duplicate products simulated";
        }
        if (ctx.data.containsKey("missingProducts")) {
            return "Missing products simulated";
        }
        if (ctx.data.containsKey("staleProductData")) {
            return "Stale product data simulated";
        }
        if (ctx.data.containsKey("duplicateCartItems")) {
            return "Duplicate cart items simulated";
        }
        if (ctx.data.containsKey("missingCartItems")) {
            return "Missing cart items simulated";
        }
        if (ctx.data.containsKey("paymentSuccessButNotSaved")) {
            return "Payment success not saved simulated";
        }
        if (ctx.data.containsKey("doublePayment")) {
            return "Double payment simulated";
        }
        if (ctx.data.containsKey("statusMismatch")) {
            return "Payment status mismatch simulated";
        }
        if (ctx.data.containsKey("duplicatePayments")) {
            return "Duplicate payments simulated";
        }
        if (ctx.data.containsKey("missingPayments")) {
            return "Missing payments simulated";
        }
        return "State inconsistency simulated for " + ctx.operation;
    }
}
