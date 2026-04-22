package com.Dummy.demo.service;

import java.util.List;

import com.Dummy.demo.model.Order;
import java.util.ArrayList;
import com.Dummy.demo.model.cartItem;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.Dummy.demo.service.internalSimulation.InternalErrorSimulator;
import com.Dummy.demo.service.internalSimulation.Context;

@Service
public class orderService {
    @Autowired
    cartService cartService;
    @Autowired
    InternalErrorSimulator internalErrorSimulator;
    List<Order> orderList = new ArrayList<>();
    private int orderIdCounter = 1;

    public String createOrder() {
        Context ctx = new Context();
        ctx.service = "order";
        ctx.operation = "ORDER_CREATE";
        internalErrorSimulator.inject(ctx.operation, ctx);

        Double total = 0.0;
        for (cartItem item : cartService.getCart()) {
            total = total + (item.getPrice() * item.getQuantity());
        }
        orderList.add(new Order(orderIdCounter, new ArrayList<>(cartService.getCart()), total, "PaymentPending"));
        orderIdCounter++;
        return "Order created successfully";
    }

    public List<Order> getOrders() {
        Context ctx = new Context();
        ctx.service = "order";
        ctx.operation = "ORDER_FETCH";
        internalErrorSimulator.inject(ctx.operation, ctx);
        return orderList;
    }

    public String deleteOrder(int id) {
        Context ctx = new Context();
        ctx.service = "order";
        ctx.operation = "ORDER_DELETE";
        internalErrorSimulator.inject(ctx.operation, ctx);

        for (Order order : orderList) {
            if (order.getOrderId() == id) { // Anything model related,like to access model properties,they are present
                                            // in model class
                orderList.remove(order);
                return "Order deleted successfully";
            }
        }
        return "Order not found";
    }
}
