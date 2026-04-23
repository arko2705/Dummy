package com.Dummy.demo.service;

import java.util.List;

import com.Dummy.demo.model.Order;
import java.util.ArrayList;
import com.Dummy.demo.model.cartItem;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import com.Dummy.demo.monitoring.service.RequestMetricsService;
import com.Dummy.demo.service.Simulation.internalSimulation.Context;
import com.Dummy.demo.service.Simulation.internalSimulation.InternalErrorSimulator;
import com.Dummy.demo.service.externalDependency.client.fakeDBClient;
import com.Dummy.demo.service.externalDependency.client.paymentGatewayClient;
import com.Dummy.demo.service.externalDependency.client.thirdPartyAPIClient;
import com.Dummy.demo.service.externalDependency.util.ReqBuilder;

@Service
public class orderService {
    @Autowired
    cartService cartService;
    @Autowired
    InternalErrorSimulator internalErrorSimulator;
    @Autowired
    RequestMetricsService metricsService;
    @Autowired
    private fakeDBClient dbClient;
    @Autowired
    private paymentGatewayClient paymentGatewayClient;
    @Autowired
    private thirdPartyAPIClient apiClient;
    private ReqBuilder reqBuilder;

    public orderService(ReqBuilder reqBuilder) {
        this.reqBuilder = reqBuilder;
    }

    List<Order> orderList = new ArrayList<>();
    private int orderIdCounter = 1;

    public String createOrder() {// wanna implement stale data thing. but its alr kind of done by dbclient
        try {
            dbClient.fetchData(reqBuilder.buildDepRequest("DB", "ORDER_CREATE"));
        } catch (Exception e) {
            System.out.println("DB Client Error: " + e.getMessage());
            throw e;
        }
        try {
            apiClient.callAPI(reqBuilder.buildDepRequest("API", "DELIVERY_ESTIMATE"));
        } catch (Exception e) {
            System.out.println("API Client Error: Failed to load delivery estimate" + e.getMessage());

        }
        try {
            paymentGatewayClient.processPayment(reqBuilder.buildDepRequest("PAYMENT", "PAYMENT_PRECHECK"));
        } catch (Exception e) {
            System.out.println("Payment Gateway Client Error: " + e.getMessage());
            throw e;
        }
        Context ctx = new Context();
        ctx.service = "order";
        ctx.operation = "ORDER_CREATE";
        ctx.data = new HashMap<>();
        try {
            internalErrorSimulator.inject(ctx.operation, ctx);
            if (ctx.data.get("emptyOrder") != null) {// Stavya and animesh do your thing bro.
                System.out.println("Empty Order Created!");
                orderList.add(new Order(orderIdCounter++, new ArrayList<>(), 0.0, "PaymentPending"));
                return "Order created successfully";
            }
            Double total = 0.0;
            for (cartItem item : cartService.getCart()) {
                total = total + (item.getPrice() * item.getQuantity());
            }
            orderList.add(new Order(orderIdCounter, new ArrayList<>(cartService.getCart()), total, "PaymentPending"));
            orderIdCounter++;
            return "Order created successfully";
        } catch (RuntimeException e) {
            // 🔥 SYSTEM DOWN
            if (e.getMessage().equals("SYSTEM_DOWN")) {
                metricsService.markSystemDown(); // NEW
                throw e;
            }

            // ⚠️ OPERATION FAILED
            throw e;// for now just propagate, reliability handles later
        }
    }

    public List<Order> getOrders() {
        try {
            dbClient.fetchData(reqBuilder.buildDepRequest("DB", "ORDER_FETCH"));
        } catch (Exception e) {
            System.out.println("DB Client Error: " + e.getMessage());
            throw e;
        }
        Context ctx = new Context();
        ctx.service = "order";
        ctx.operation = "ORDER_FETCH";
        ctx.data = new HashMap<>();
        try {
            internalErrorSimulator.inject(ctx.operation, ctx);// stavya and animesh do your thing bbgs
            if (ctx.data.get("missingOrders") != null) {
                System.out.println("Missing Orders!");
                return orderList.subList(0, orderList.size() / 2);
            }

            else if (ctx.data.get("duplicateOrders") != null) {
                System.out.println("Duplicate Orders!");
                List<Order> temp = new ArrayList<>(orderList);
                temp.addAll(orderList);
                return temp;
            }

            else if (ctx.data.get("wrongOrderStatus") != null) {
                System.out.println("Wrong Order Status!");
                List<Order> temp = new ArrayList<>(orderList);
                for (Order o : temp) {
                    o.setStatus("DELIVERED"); // wrong
                }
                return temp;
            }
            return orderList;
        } catch (RuntimeException e) {
            // 🔥 SYSTEM DOWN
            if (e.getMessage().equals("SYSTEM_DOWN")) {
                metricsService.markSystemDown(); // NEW
                throw e;
            }

            // ⚠️ OPERATION FAILED
            throw e;
        }
    }

    public String deleteOrder(int id) {
        try {
            dbClient.fetchData(reqBuilder.buildDepRequest("DB", "ORDER_DELETE"));
        } catch (Exception e) {
            System.out.println("DB Client Error: " + e.getMessage());
            throw e;
        }
        try {
            paymentGatewayClient.processPayment(reqBuilder.buildDepRequest("PAYMENT", "PAYMENT_REFUND"));
        } catch (Exception e) {
            System.out.println("Payment Gateway Client Error: Could'nt process refund" + e.getMessage());
            throw e;
        }
        Context ctx = new Context();
        ctx.service = "order";
        ctx.operation = "ORDER_DELETE";
        try {
            internalErrorSimulator.inject(ctx.operation, ctx);
            for (Order order : orderList) {
                if (order.getOrderId() == id) { // Anything model related,like to access model properties,they are
                                                // present
                                                // in model class
                    orderList.remove(order);
                    return "Order deleted successfully";
                }
            }
            return "Order not found";
        } catch (RuntimeException e) {
            // 🔥 SYSTEM DOWN
            if (e.getMessage().equals("SYSTEM_DOWN")) {
                metricsService.markSystemDown(); // NEW
                throw e;
            }

            // ⚠️ OPERATION FAILED
            throw e;// reliability handles this later,for now just propagate
        }
    }
}
