package com.Dummy.demo.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import com.Dummy.demo.service.productService;
import com.Dummy.demo.model.Product;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/A")
    public String productA() {
        return "List of product under A";
    }

    @GetMapping("/B")
    public String productB() {
        return "List of products under B";
    }

    @GetMapping("/C")
    public String productC() {
        return "List of products under C";
    }

}
