package com.Dummy.demo.DTO;

public class PaymentRequest {

    private int orderId;
    private String method;

    public PaymentRequest() {}

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
