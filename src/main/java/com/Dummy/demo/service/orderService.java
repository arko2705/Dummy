package com.Dummy.demo.service;

import java.util.List;

import com.Dummy.demo.model.Order;
import java.util.ArrayList;
import com.Dummy.demo.model.cartItem;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.Dummy.demo.service.internalSimulation.InternalErrorSimulator;
import com.Dummy.demo.service.internalSimulation.Context;
import java.util.HashMap;
import com.Dummy.demo.monitoring.service.RequestMetricsService;

@Service
public class orderService {
    @Autowired
    cartService cartService;
    @Autowired
    InternalErrorSimulator internalErrorSimulator;
    @Autowired
    RequestMetricsService metricsService;
    List<Order> orderList = new ArrayList<>();
    private int orderIdCounter = 1;

    public String createOrder() {// wanna implement stale data thing
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
