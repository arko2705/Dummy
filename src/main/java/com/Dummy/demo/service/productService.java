package com.Dummy.demo.service;

import com.github.javafaker.Faker;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.Dummy.demo.model.Product;
import java.util.ArrayList;
import java.util.List;
import com.Dummy.demo.service.internalSimulation.InternalErrorSimulator;
import com.Dummy.demo.service.internalSimulation.Context;

@Service
public class productService {
    @Autowired
    InternalErrorSimulator internalErrorSimulator;
    Faker fakeProd = new Faker();
    public List<Product> prodList = new ArrayList<>();

    public productService() {
    }

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
        Context ctx = new Context();
        ctx.service = "product";
        ctx.operation = "PRODUCT_ADD";
        internalErrorSimulator.inject("PRODUCT_ADD", ctx);
        if (name == null || name.isEmpty() || price == null) {
            return "Name and price cannot be empty.";
        }
        Integer id = prodList.size() + 1;
        prodList.add(new Product(id, name, price));
        return "Product added successfully.";
    }

    public List<Product> getList(String search, Integer size) {
        Context ctx = new Context();
        ctx.service = "product";
        ctx.operation = "PRODUCT_FETCH";
        internalErrorSimulator.inject("PRODUCT_FETCH", ctx);
        try {
            Thread.sleep(2000); // Im waitng for 4 seconds to wait for all requests to reach at once.Basically
                                // this is put after incrementLoad(),so that it increases count of current
                                // requests while the others are still processing for 5 seconds.
        } catch (InterruptedException e) {
            System.out.println("some error");
        }
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
        Context ctx = new Context();
        ctx.service = "product";
        ctx.operation = "PRODUCT_UPDATE";
        internalErrorSimulator.inject(ctx.operation, ctx);

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
        Context ctx = new Context();
        ctx.service = "product";
        ctx.operation = "PRODUCT_DELETE";
        internalErrorSimulator.inject(ctx.operation, ctx);
        for (Product p : prodList) {
            if (p.getId() == id) {
                prodList.remove(p);
                return "Product deleted successfully";
            }
        }
        return "Product not found";
    }
}
