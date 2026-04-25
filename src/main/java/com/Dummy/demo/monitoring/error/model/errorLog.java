package com.Dummy.demo.monitoring.error.model;

public class errorLog {
    public long timestamp;
    public String service;
    public String operation;
    public String errorType;   // SYSTEM_DOWN / DB_FAILED / etc
    public String message;
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String getService() {
        return service;
    }
    public void setService(String service) {
        this.service = service;
    }
    public String getOperation() {
        return operation;
    }
    public void setOperation(String operation) {
        this.operation = operation;
    }
    public String getErrorType() {
        return errorType;
    }
    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}