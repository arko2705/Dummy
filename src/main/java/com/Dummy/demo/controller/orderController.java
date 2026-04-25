package com.Dummy.demo.controller;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.Dummy.demo.model.Order;
import com.Dummy.demo.service.orderService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management APIs")
public class orderController {
    orderService orderservice;

    public orderController(orderService orderservice) {
        this.orderservice = orderservice;
    }

    @GetMapping
    public List<Order> getOrders(HttpServletRequest request) {
        return orderservice.getOrders(request);
    }

    @PostMapping("/create")
    public String createOrder(HttpServletRequest request) {
        return orderservice.createOrder(request);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteOrder(@PathVariable int id) {
        // Implementation for deleting an order
        return orderservice.deleteOrder(id);
    }
}
