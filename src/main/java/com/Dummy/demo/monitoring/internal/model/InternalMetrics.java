package com.Dummy.demo.monitoring.internal.model;

public class InternalMetrics {
    private int processingDelayEvents;
    private int stateInconsistencyEvents;
    private int systemCrashEvents;
    private int concurrencyEvents;

    public InternalMetrics() {
    }

    public InternalMetrics(int processingDelayEvents, int stateInconsistencyEvents, int systemCrashEvents,
            int concurrencyEvents) {
        this.processingDelayEvents = processingDelayEvents;
        this.stateInconsistencyEvents = stateInconsistencyEvents;
        this.systemCrashEvents = systemCrashEvents;
        this.concurrencyEvents = concurrencyEvents;
    }

    public int getProcessingDelayEvents() {
        return processingDelayEvents;
    }

    public void setProcessingDelayEvents(int processingDelayEvents) {
        this.processingDelayEvents = processingDelayEvents;
    }

    public int getStateInconsistencyEvents() {
        return stateInconsistencyEvents;
    }

    public void setStateInconsistencyEvents(int stateInconsistencyEvents) {
        this.stateInconsistencyEvents = stateInconsistencyEvents;
    }

    public int getSystemCrashEvents() {
        return systemCrashEvents;
    }

    public void setSystemCrashEvents(int systemCrashEvents) {
        this.systemCrashEvents = systemCrashEvents;
    }

    public int getConcurrencyEvents() {
        return concurrencyEvents;
    }

    public void setConcurrencyEvents(int concurrencyEvents) {
        this.concurrencyEvents = concurrencyEvents;
    }
}
