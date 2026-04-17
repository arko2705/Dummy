package com.Dummy.demo.service.externalDependency.model;

public class externalResponse {

    private String requestId;
    private String dependencyType;
    private boolean success;
    private String message;
    private Object data;

    public externalResponse(String requestId, String dependencyType,
            boolean success, String message, Object data) {
        this.requestId = requestId;
        this.dependencyType = dependencyType;
        this.success = success;
        this.message = message;// error/success info
        this.data = data;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getDependencyType() {
        return dependencyType;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

}
