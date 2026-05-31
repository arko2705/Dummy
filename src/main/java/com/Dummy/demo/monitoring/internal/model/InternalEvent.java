package com.Dummy.demo.monitoring.internal.model;

public class InternalEvent {
    private InternalEventType eventType;
    private long timestamp;
    private String operation;
    private String service;

    public InternalEvent() {
    }

    public InternalEvent(InternalEventType eventType, long timestamp, String operation, String service) {
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.operation = operation;
        this.service = service;
    }

    public InternalEventType getEventType() {
        return eventType;
    }

    public void setEventType(InternalEventType eventType) {
        this.eventType = eventType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}
