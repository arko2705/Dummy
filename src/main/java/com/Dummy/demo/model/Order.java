package com.Dummy.demo.model;

import java.util.List;

//Verdict: Call it a DTO or a model—both work for a database-less app. The structure is perfectly fine for carrying data between your controller, service, and client. You don't need separate "DTO" and "model" folders when there's no database. One folder is enough.
public class Order {
    private int orderId;
    private List<cartItem> items;
    private double total;
    private String status;

    public Order(int orderId, List<cartItem> items, double total, String status) {
        this.orderId = orderId;
        this.items = items;
        this.total = total;
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public List<cartItem> getItems() {
        return items;
    }

    public double getTotal() {
        return total;
    }

    public String getStatus() {
        return status;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public void setItems(List<cartItem> items) {
        this.items = items;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
