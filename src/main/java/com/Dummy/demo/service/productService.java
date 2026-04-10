package com.Dummy.demo.service;

import com.github.javafaker.Faker;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import com.Dummy.demo.model.Product;
import java.util.ArrayList;
import java.util.List;

@Service
public class productService {
    Faker fakeProd = new Faker();
    public List<Product> prodList = new ArrayList<>();

    @PostConstruct // makes this certain method run after service,controller,and rest of the beans
                   // created
    public void generateProdList() {
        for (Integer i = 0; i < 150; i++) {
            Integer id = i + 1;
            String name = fakeProd.commerce().productName();
            Double fakePrice = Double.parseDouble(fakeProd.commerce().price()); // price between 0 and 1000
            prodList.add(new Product(id, name, fakePrice));
        }
    }

    public String addProduct(String name, Double price) {
        if (name == null || name.isEmpty() || price == null) {
            return "Name and price cannot be empty.";
        }
        Integer id = prodList.size() + 1;
        prodList.add(new Product(id, name, price));
        return "Product added successfully.";
    }

    public List<Product> getList(String search, Integer size) {

        List<Product> result = new ArrayList<>();
        // Start with full list if no search
        if (search == null || search.isEmpty()) {
            result = new ArrayList<>(prodList);
        } else {
            for (Product prod : prodList) {
                if (prod.getName().toLowerCase().contains(search.toLowerCase())) {
                    result.add(prod);
                }
            }
        }
        // Apply size limit
        if (size != null && size < result.size()) {
            List<Product> limited = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                limited.add(result.get(i));
            }
            return limited;
        }
        return result;
    }

    public String updateProd(int id, String reqName, Double reqPrice) {
        for (Product p : prodList) {
            if (p.getId() == id) {
                p.setName(reqName);
                p.setPrice(reqPrice);
                return "Product updated successfully";
            }
        }
        return "Product not found";
    }

    public String delProd(int id) {
        for (Product p : prodList) {
            if (p.getId() == id) {
                prodList.remove(p);
                return "Product deleted successfully";
            }
        }
        return "Product not found";
    }
}
