package com.Dummy.demo.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import com.Dummy.demo.service.productService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import com.Dummy.demo.model.Product;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product catalog APIs")

public class productController {
    productService productService;

    public productController(productService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getProducts(@RequestParam(required = false) String search,
            @RequestParam(required = false) Integer size,
            HttpServletRequest request) {
        return productService.getList(search, size, request);
    }

    @PostMapping("/add")
    public String addProd(@RequestBody Product prod) { // RequestBody dataStructure obv needs to be mentioned.
        return productService.addProduct(prod.getName(), prod.getPrice());
    }

    @PutMapping("/update") // PutMapping is idempotent,it doesnt allow duplicates. PostMapping isnt.
    public String updateProd(@RequestBody Product prod) {
        return productService.updateProd(prod.getId(), prod.getName(), prod.getPrice());
    }

    @DeleteMapping("/delete/{id}") // the url path
    public String deleteProd(@PathVariable int id) { // url paths variable
        return productService.delProd(id);
    }
}
