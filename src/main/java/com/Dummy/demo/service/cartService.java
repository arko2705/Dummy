package com.Dummy.demo.service;

import org.springframework.stereotype.Service;
import com.Dummy.demo.model.cartItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.Dummy.demo.model.Product;

import org.springframework.beans.factory.annotation.Autowired;
import com.Dummy.demo.monitoring.service.RequestMetricsService;
import com.Dummy.demo.service.Simulation.internalSimulation.Context;
import com.Dummy.demo.service.Simulation.internalSimulation.InternalErrorSimulator;
import com.Dummy.demo.service.externalDependency.client.fakeDBClient;
import com.Dummy.demo.service.externalDependency.client.thirdPartyAPIClient;
import com.Dummy.demo.service.externalDependency.util.ReqBuilder;

@Service
public class cartService {
    @Autowired
    productService prodService;
    @Autowired
    InternalErrorSimulator internalErrorSimulator;
    @Autowired
    RequestMetricsService metricsService;
    @Autowired
    private fakeDBClient dbClient;
    @Autowired
    private thirdPartyAPIClient apiClient;
    private ReqBuilder reqBuilder;

    public cartService(ReqBuilder reqBuilder) {
        this.reqBuilder = reqBuilder;
    }

    List<cartItem> itemList = new ArrayList<>();

    public List<cartItem> getCart() {
        try {
            dbClient.fetchData(reqBuilder.buildDepRequest("DB", "CART_FETCH"));
        } catch (Exception e) {
            System.out.println("DB Client Error:Coudlnt fetch cart from DB " + e.getMessage());
            throw e;
        }
        Context ctx = new Context();
        ctx.service = "cart";
        ctx.operation = "CART_FETCH";
        ctx.data = new HashMap<>();
        try {
            internalErrorSimulator.inject(ctx.operation, ctx);
            if (ctx.data.get("missingCartItems") != null) {// newer errors i will make,unique errors. StaleCart is a
                                                           // really
                                                           // good concept,i dont have time right now.
                return itemList.subList(0, itemList.size() / 2);
            }

            if (ctx.data.get("duplicateCartItems") != null) {
                List<cartItem> temp = new ArrayList<>(itemList);
                temp.addAll(itemList);
                return temp;
            }
            return itemList;
        } catch (RuntimeException e) {
            // ⚠️ OPERATION FAILED
            throw e;// reliability handles this later,for now just propagate
        }
    }

    public String addToCart(Integer id, int quantity) {
        try {
            dbClient.fetchData(reqBuilder.buildDepRequest("DB", "CART_ADD"));
        } catch (Exception e) {
            System.out.println("DB Client Error: Could'nt add to cart in DB " + e.getMessage());
            throw e;
        }
        try {
            apiClient.callAPI(reqBuilder.buildDepRequest("API", "INVENTORY_CHECK"));
        } catch (Exception e) {
            System.out.println("API Client Error: Could'nt check inventory " + e.getMessage());
            throw e;
        }
        Context ctx = new Context();// one context fine for multiple strategies,all multiples strategies attached to
                                    // that and in inject it is looped over.
        ctx.service = "cart";
        ctx.operation = "CART_ADD";
        ctx.data = new HashMap<>();
        try {
            internalErrorSimulator.inject(ctx.operation, ctx);
            if (ctx.data.get("productDeleted") != null) {// hashMap for this particular context checked.
                return "Product suddenly unavailable (simulated inconsistency)";
            } else if (ctx.data.get("wrongQuantity") != null) {// again bro stavya do your thing.
                quantity += 1;
            }
            if (itemList.size() > 0) {
                for (cartItem i : itemList) {
                    if (i.getId() == id) {
                        i.setQuantity(i.getQuantity() + quantity);
                        return "Item quantity updated successfully.";
                    }
                }
            }
            for (Product i : prodService.getList(null, null)) {
                if (i.getId().equals(id)) { // Had to do .equals() to compare values,else its some bullshit Integer
                                            // objects
                                            // have a rule regarding,that beyond beyond 127 it compares references or sm
                                            // shi.Beyond 127 it creates new objects and compares references,whihc ofc
                                            // isnt
                                            // equal
                    itemList.add(new cartItem(i.getId(), i.getName(), i.getPrice(), quantity));
                    return "New Item added to cart successfully.";
                }
            }
            return "Product not found.";
        } catch (RuntimeException e) {
            // ⚠️ OPERATION FAILED
            throw e;// reliability handles this later,for now just propagate
        }
    }

    public String delCartItem(int id) {
        try {
            dbClient.fetchData(reqBuilder.buildDepRequest("DB", "CART_DELETE"));
        } catch (Exception e) {
            System.out.println("DB Client Error: Could'nt delete item from cart " + e.getMessage());
            throw e;
        }
        Context ctx = new Context();
        ctx.service = "cart";
        ctx.operation = "CART_DELETE";
        try {
            internalErrorSimulator.inject(ctx.operation, ctx);
            for (cartItem i : itemList) {
                if (i.getId().equals(id)) {
                    itemList.remove(i);
                    return "Item removed from cart successfully.";
                }
            }
            return "Item not found in cart.";
        } catch (RuntimeException e) {
            // 🔥 SYSTEM DOWN
            if (e.getMessage().equals("SYSTEM_DOWN")) {
                metricsService.markSystemDown(); // NEW
                throw e;
            }

            // ⚠️ OPERATION FAILED
            // metricsService.recordFailure(); // This is already handled by the interceptor
            throw e;// reliability handles this later,for now just propagate
        }
    }
}
// bro i think clarity was hit. Till now,i was getting confused whats getting
// displayed on the getCart page right,cuz yeah initially i was like yeah i pass
// id and quantity,ion pass the names. But then carItem becomes
// id=sm,name=null,price=null and quantity=something as well. cartItem is just a
// pawn here, as cartItem is then later used to access product in
// cartService,where it creates a List of cartItems,and initialises an item in
// the line new cartItem(i.getId(), i.getName(), i.getPrice(), quantity));