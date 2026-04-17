package com.Dummy.demo.service.externalDependency.model;

import java.util.Map;

public class externalRequest {

    private String requestId;
    private String dependencyType; // DB, PAYMENT, THIRD_PARTY
    private Map<String, Object> payload;

    public externalRequest(String requestId, String dependencyType, Map<String, Object> payload) {
        this.requestId = requestId;
        this.dependencyType = dependencyType;
        this.payload = payload;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getDependencyType() {
        return dependencyType;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }
}
