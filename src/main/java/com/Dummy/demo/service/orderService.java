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

import jakarta.servlet.http.HttpServletRequest;

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

    //passing an HttpServletRequest inorder to attatch the context to the global exception handler where it can extract data from the context
    public String createOrder(HttpServletRequest request) {// wanna implement stale data thing. but its alr kind of done by dbclient
        try {
            dbClient.fetchData(reqBuilder.buildDepRequest("DB", "ORDER_CREATE"));
        } catch (Exception e) {
            throw e;
        }
        try {
            apiClient.callAPI(reqBuilder.buildDepRequest("API", "DELIVERY_ESTIMATE"));
        } catch (Exception e) {
            //throw e;        //Apparently we are not supposed to throw and exception for callAPI functions?
        }
        try {
            paymentGatewayClient.processPayment(reqBuilder.buildDepRequest("PAYMENT", "PAYMENT_PRECHECK"));
        } catch (Exception e) {
            throw e;
        }
        Context ctx = new Context();
        ctx.service = "order";
        ctx.operation = "ORDER_CREATE";
        ctx.data = new HashMap<>();
        request.setAttribute("service", ctx.service);
        request.setAttribute("operation", ctx.operation);
        try {
            internalErrorSimulator.inject(ctx.operation, ctx);
            if (ctx.data.get("emptyOrder") != null) {// Stavya and animesh do your thing bro.
                System.out.println("Empty Order Created!");
                orderList.add(new Order(orderIdCounter++, new ArrayList<>(), 0.0, "PaymentPending"));
                return "Order created successfully";
            }
            Double total = 0.0;
            for (cartItem item : cartService.getCartInternal()) {
                total = total + (item.getPrice() * item.getQuantity());
            }
            orderList.add(new Order(orderIdCounter, new ArrayList<>(cartService.getCartInternal()), total, "PaymentPending"));
            orderIdCounter++;
            return "Order created successfully";
        } catch (RuntimeException e) {
            throw e;// for now just propagate, reliability handles later
        }
    }

    // CORE
    private List<Order> getOrdersCore(Context ctx) {
        internalErrorSimulator.inject(ctx.operation, ctx);

        if (ctx.data.get("missingOrders") != null) {
            return orderList.subList(0, orderList.size() / 2);
        }

        if (ctx.data.get("duplicateOrders") != null) {
            List<Order> temp = new ArrayList<>(orderList);
            temp.addAll(orderList);
            return temp;
        }

        if (ctx.data.get("wrongOrderStatus") != null) {
            List<Order> temp = new ArrayList<>(orderList);
            for (Order o : temp) {
                o.setStatus("DELIVERED");
            }
            return temp;
        }

        return orderList;
    }

    // CONTROLLER VERSION
    public List<Order> getOrders(HttpServletRequest request) {
        dbClient.fetchData(reqBuilder.buildDepRequest("DB", "ORDER_FETCH"));

        Context ctx = new Context();
        ctx.service = "order";
        ctx.operation = "ORDER_FETCH";
        ctx.data = new HashMap<>();

        request.setAttribute("service", ctx.service);
        request.setAttribute("operation", ctx.operation);

        return getOrdersCore(ctx);
    }

    // INTERNAL VERSION
    public List<Order> getOrdersInternal() {
        dbClient.fetchData(reqBuilder.buildDepRequest("DB", "ORDER_FETCH"));

        Context ctx = new Context();
        ctx.service = "order";
        ctx.operation = "ORDER_FETCH";
        ctx.data = new HashMap<>();

        return getOrdersCore(ctx);
    }

    public String deleteOrder(int id) {
        try {
            dbClient.fetchData(reqBuilder.buildDepRequest("DB", "ORDER_DELETE"));
        } catch (Exception e) {
            throw e;
        }
        try {
            paymentGatewayClient.processPayment(reqBuilder.buildDepRequest("PAYMENT", "PAYMENT_REFUND"));
        } catch (Exception e) {
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
            //for every error injection in the service layer we remove wherever we record an error. Because it is not service's job to do so. Globla exception handler should handle it.
            throw e;
        }
    }
}
