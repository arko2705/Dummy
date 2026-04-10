package com.Dummy.demo.service;

import java.util.List;

import com.Dummy.demo.model.Order;
import java.util.ArrayList;
import com.Dummy.demo.model.cartItem;
import org.springframework.stereotype.Service;

@Service
public class orderService {
    cartService cartService;

    public orderService(cartService cartService) {
        this.cartService = cartService;
    }

    List<Order> orderList = new ArrayList<>();
    private int orderIdCounter = 1;

    public String createOrder() {
        Double total = 0.0;
        for (cartItem item : cartService.getCart()) {
            total = total + (item.getPrice() * item.getQuantity());
        }
        orderList.add(new Order(orderIdCounter, new ArrayList<>(cartService.getCart()), total, "Created"));
        orderIdCounter++;
        return "Order created successfully";
    }

    public List<Order> getOrders() {
        return orderList;
    }
}
