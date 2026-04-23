package com.Dummy.demo.service.externalDependency.util;

import com.Dummy.demo.service.externalDependency.model.externalRequest;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ReqBuilder {

    public externalRequest buildDepRequest(String dep, String op) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("operation", op);

        return new externalRequest(
                "req-" + System.currentTimeMillis(),
                dep, // "DB", "PAYMENT", "API"
                payload);
    }
}