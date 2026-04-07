package com.Dummy.demo.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;

import com.Dummy.demo.model.cartItem;
import com.Dummy.demo.service.cartService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/cart")
public class cartController {
    cartService cartservice;

    public cartController(cartService cartservice) {
        this.cartservice = cartservice;
    }

    @GetMapping
    public List<cartItem> getCart() {
        return cartservice.getCart();
    }

    @PostMapping("/add")
    public String addToCart(@RequestBody cartItem cartitem) {
        return cartservice.addToCart(cartitem.getId(), cartitem.getQuantity());
    }
}
