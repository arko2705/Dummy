package com.Dummy.demo.service;

import com.github.javafaker.Faker;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.Dummy.demo.model.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import com.Dummy.demo.monitoring.service.RequestMetricsService;
import com.Dummy.demo.service.Simulation.internalSimulation.Context;
import com.Dummy.demo.service.Simulation.internalSimulation.InternalErrorSimulator;
import com.Dummy.demo.service.externalDependency.client.fakeDBClient;
import com.Dummy.demo.service.externalDependency.client.thirdPartyAPIClient;
import com.Dummy.demo.service.externalDependency.util.ReqBuilder;

//the try catch if else blocks handle the Internal errors.Go to InternalErrorSimulator
//api error is non fatal,but db and payment ones are.
@Service
public class productService {
    @Autowired
    InternalErrorSimulator internalErrorSimulator;
    @Autowired
    RequestMetricsService metricsService;
    @Autowired
    private fakeDBClient dbClient;
    @Autowired
    private thirdPartyAPIClient apiClient;
    Faker fakeProd = new Faker();
    public List<Product> prodList = new ArrayList<>();
    private ReqBuilder reqBuilder;

    public productService(ReqBuilder reqBuilder) {
        this.reqBuilder = reqBuilder;
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
        dbClient.fetchData(reqBuilder.buildDepRequest("DB", "PRODUCT_ADD"));// this causes stale data,random error and
                                                                            // crash.While our internal causes similar
                                                                            // things too,GPT said its not
                                                                            // redundant,they are seperate. Processing
                                                                            // delay is differnt from dbLatency,so and
                                                                            // so

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
            throw e;// for now just propagate, globalExceptionHandler handles later
        }
    }

    // CORE
    private List<Product> getListCore(String search, Integer size, Context ctx) {
        internalErrorSimulator.inject(ctx.operation, ctx);

        List<Product> result = new ArrayList<>();

        if (search == null || search.isEmpty()) {
            result = new ArrayList<>(prodList);
        } else {
            for (Product prod : prodList) {
                if (prod.getName().toLowerCase().contains(search.toLowerCase())) {
                    result.add(prod);
                }
            }
        }

        if (size != null && size < result.size()) {
            return result.subList(0, size);
        }

        if (ctx.data.get("missingProducts") != null) {
            return result.subList(0, result.size() / 2);
        }

        if (ctx.data.get("duplicateProducts") != null) {
            List<Product> temp = new ArrayList<>(result);
            temp.addAll(result);
            return temp;
        }

        if (ctx.data.get("staleProductData") != null) {
            for (Product p : result) {
                p.setPrice(p.getPrice() - 10);
            }
            return result;
        }

        return result;
    }

    // CONTROLLER VERSION
    public List<Product> getList(String search, Integer size, HttpServletRequest request) {
        dbClient.fetchData(reqBuilder.buildDepRequest("DB", "PRODUCT_FETCH"));
        try {
            apiClient.callAPI(reqBuilder.buildDepRequest("API", "PRODUCT_RECOMMEND"));
            apiClient.callAPI(reqBuilder.buildDepRequest("API", "PRODUCT_RATING"));
        } catch (Exception ignored) {}

        Context ctx = new Context();
        ctx.service = "product";
        ctx.operation = "PRODUCT_FETCH";
        ctx.data = new HashMap<>();

        request.setAttribute("service", ctx.service);
        request.setAttribute("operation", ctx.operation);

        return getListCore(search, size, ctx);
    }

    // INTERNAL VERSION
    public List<Product> getListInternal(String search, Integer size) {
        dbClient.fetchData(reqBuilder.buildDepRequest("DB", "PRODUCT_FETCH"));
        try {
            apiClient.callAPI(reqBuilder.buildDepRequest("API", "PRODUCT_RECOMMEND"));
            apiClient.callAPI(reqBuilder.buildDepRequest("API", "PRODUCT_RATING"));
        } catch (Exception ignored) {}

        Context ctx = new Context();
        ctx.service = "product";
        ctx.operation = "PRODUCT_FETCH";
        ctx.data = new HashMap<>();

        return getListCore(search, size, ctx);
    }

    public String updateProd(int id, String reqName, Double reqPrice) {// gotta add these as errors cuz error count
                                                                       // affected

        try {
            dbClient.fetchData(reqBuilder.buildDepRequest("DB", "PRODUCT_UPDATE"));
        } catch (Exception e) {
            throw e;
        }
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
            throw e;
        }

    }

    public String delProd(int id) {// when an error thrown,none of the rest of the code will work. The code exits
                                   // the method
        try {
            dbClient.fetchData(reqBuilder.buildDepRequest("DB", "PRODUCT_DELETE"));
        } catch (Exception e) {
            throw e;
        }
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
            throw e;
        }
    }
}
