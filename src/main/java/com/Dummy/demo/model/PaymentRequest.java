package com.Dummy.demo.model;

//A DTO (Data Transfer Object) is a simple Java object used to carry data between processes, often between a controller and service or client and server. It typically contains only fields, getters/setters, and no business logic.
public class PaymentRequest {

    private int orderId;
    private String method;

    public PaymentRequest() {
    }

    public PaymentRequest(int orderId, String method) {
        this.orderId = orderId;
        this.method = method;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
