package com.Dummy.demo.monitoring.dependency.model;

public class DependencyEvent {
    private String dependencyName;
    private long latency;
    private boolean success;
    private long timestamp;

    public DependencyEvent() {
    }

    public DependencyEvent(String dependencyName, long latency, boolean success, long timestamp) {
        this.dependencyName = dependencyName;
        this.latency = latency;
        this.success = success;
        this.timestamp = timestamp;
    }

    public String getDependencyName() {
        return dependencyName;
    }

    public void setDependencyName(String dependencyName) {
        this.dependencyName = dependencyName;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

