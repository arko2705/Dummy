package com.Dummy.demo.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.Dummy.demo.model.cartItem;
import com.Dummy.demo.service.cartService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;

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

    @DeleteMapping("/delete/{id}")
    public String delCartItem(@PathVariable int id) {
        return cartservice.delCartItem(id);
    }
}
