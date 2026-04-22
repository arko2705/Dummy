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
import java.util.HashMap;
import com.Dummy.demo.monitoring.service.RequestMetricsService;

@Service
public class productService {
    @Autowired
    InternalErrorSimulator internalErrorSimulator;
    @Autowired
    RequestMetricsService metricsService;
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
        ctx.data = new HashMap<>();
        try {
            internalErrorSimulator.inject("PRODUCT_ADD", ctx);
            if (ctx.data.get("systemDown") != null) {
                throw new RuntimeException("System is down");
            }
            if (ctx.data.get("operationFailed") != null) {
                return "Failed to process request";
            }
            Integer id = prodList.size() + 1;
            prodList.add(new Product(id, name, price));
            return "Product added successfully.";
        } catch (RuntimeException e) {
            // 🔥 SYSTEM DOWN
            if (e.getMessage().equals("SYSTEM_DOWN")) {
                metricsService.markSystemDown(); // NEW
                throw e;
            }

            // ⚠️ OPERATION FAILED
            metricsService.recordFailure();
            throw e;// for now just propagate, reliability handles later
        }
    }

    public List<Product> getList(String search, Integer size) {
        Context ctx = new Context();
        ctx.service = "product";
        ctx.operation = "PRODUCT_FETCH";
        ctx.data = new HashMap<>();
        try {
            internalErrorSimulator.inject(ctx.operation, ctx);

            // try {
            // Thread.sleep(2000); // Im waitng for 4 seconds to wait for all requests to
            // reach at once.Basically
            // // this is put after incrementLoad(),so that it increases count of current
            // // requests while the others are still processing for 5 seconds.
            // } catch (InterruptedException e) {
            // System.out.println("some error");
            // }
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

            if (ctx.data.get("missingProducts") != null) {// errors,need to log. Since i have actually changed the
                                                          // return
                                                          // data instead of sayng that error has been made,see if there
                                                          // is
                                                          // some clever way for the ML to interpret the results and see
                                                          // that this is wrong(eg SLO and actual ouput comparison),else
                                                          // we
                                                          // will just revert back to logs since i think thats
                                                          // unnecessary
                                                          // processing. Like lets say duplicate causes sudden spike in
                                                          // response size,etc etc.ML shld see that,not just a
                                                          // "duplicate
                                                          // products" thingy
                System.out.println("Missing PRODUCTS!");
                List<Product> temp = new ArrayList<>(result);
                return temp.subList(0, temp.size() / 2); // Simulate missing data by returning only half the results
            } else if (ctx.data.get("duplicateProducts") != null) {
                System.out.println("Duplicate PRODUCTS!");
                List<Product> temp = new ArrayList<>(result);
                temp.addAll(new ArrayList<>(temp));
                return temp;
            } else if (ctx.data.get("staleProductData") != null) {
                System.out.println("Stale PRODUCT DATA!");
                List<Product> temp = new ArrayList<>(result);
                for (Product p : temp) {
                    p.setPrice(p.getPrice() - 10); // Simulate stale data by reducing the price
                }
                return temp;
            }
            return result;

        } catch (RuntimeException e) {
            // 🔥 SYSTEM DOWN
            if (e.getMessage().equals("SYSTEM_DOWN")) {
                metricsService.markSystemDown(); // NEW
                throw e;
            }

            // ⚠️ OPERATION FAILED
            metricsService.recordFailure();
            throw e; // for now just propagate, reliability handles later
        }
    }

    public String updateProd(int id, String reqName, Double reqPrice) {// gotta add these as errors cuz error count
                                                                       // affected
        Context ctx = new Context();
        ctx.service = "product";
        ctx.operation = "PRODUCT_UPDATE";
        ctx.data = new HashMap<>();
        try {// this for the system/operation failure
            internalErrorSimulator.inject(ctx.operation, ctx);
            if (ctx.data.get("updateNotApplied") != null) {
                System.out.println("Update NOT APPLIED!");
                return "Product update successfully";// ML gotta recognise this somehow that price DIDNT get updated.
                                                     // Stavya
                                                     // all you bro
            } else if (ctx.data.get("partialUpdate") != null) {
                System.out.println("Partial Update!");
                for (Product p : prodList) {
                    if (p.getId() == id) {
                        p.setName(reqName);
                        // price update not applied
                    }
                }
                return "Product partially updated successfully";
            } else if (ctx.data.get("wrongPriceUpdate") != null) {
                System.out.println("Wrong Price Update!");
                reqPrice = reqPrice + 20;// some wrong price update
                for (Product p : prodList) {
                    if (p.getId() == id) {
                        p.setName(reqName);
                        p.setPrice(reqPrice);
                    }
                }
                return "Product updated successfully";

            }
            for (Product p : prodList) {
                if (p.getId() == id) {
                    p.setName(reqName);
                    p.setPrice(reqPrice);
                    return "Product updated successfully";
                }
            }
            return "Product not found";

        } catch (RuntimeException e) {
            // 🔥 SYSTEM DOWN
            if (e.getMessage().equals("SYSTEM_DOWN")) {
                metricsService.markSystemDown(); // NEW
                throw e;
            }

            // ⚠️ OPERATION FAILED
            metricsService.recordFailure();
            throw e; // for now just propagate, reliability handles later
        }

    }

    public String delProd(int id) {
        Context ctx = new Context();
        ctx.service = "product";
        ctx.operation = "PRODUCT_DELETE";
        try {
            internalErrorSimulator.inject(ctx.operation, ctx);
            for (Product p : prodList) {
                if (p.getId() == id) {
                    prodList.remove(p);
                    return "Product deleted successfully";
                }
            }
            return "Product not found";

        } catch (RuntimeException e) {
            // 🔥 SYSTEM DOWN
            if (e.getMessage().equals("SYSTEM_DOWN")) {
                metricsService.markSystemDown(); // NEW
                throw e;
            }

            // ⚠️ OPERATION FAILED
            metricsService.recordFailure();
            throw e; // for now just propagate, reliability handles later
        }
    }
}
