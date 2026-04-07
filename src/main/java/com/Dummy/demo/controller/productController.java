package com.Dummy.demo.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import com.Dummy.demo.service.productService;
import com.Dummy.demo.model.Product;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/products")

public class productController {
    productService productService;

    public productController(productService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getProducts(@RequestParam(required = false) String search,
            @RequestParam(required = false) Integer size) {
        return productService.getList(search, size);
    }

    @PostMapping("/add")
    public String addProd(@RequestBody Product prod) {
        return productService.addProduct(prod.getName(), prod.getPrice());
    }

}
