package com.Dummy.demo.controller;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.Dummy.demo.model.Order;
import com.Dummy.demo.service.orderService;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/orders")
public class orderController {
    orderService orderservice;

    public orderController(orderService orderservice) {
        this.orderservice = orderservice;
    }

    @GetMapping
    public List<Order> getOrders() {
        return orderservice.getOrders();
    }

    @PostMapping("/create")
    public String createOrder() {
        return orderservice.createOrder();
    }
}
