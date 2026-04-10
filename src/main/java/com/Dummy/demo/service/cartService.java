package com.Dummy.demo.service;

import org.springframework.stereotype.Service;
import com.Dummy.demo.model.cartItem;
import java.util.ArrayList;
import java.util.List;
import com.Dummy.demo.model.Product;

@Service
public class cartService {
    productService prodService;

    public cartService(productService prodService) {
        this.prodService = prodService;
    }

    List<cartItem> itemList = new ArrayList<>();

    public List<cartItem> getCart() {
        return itemList;
    }

    public String addToCart(Integer id, int quantity) {
        if (itemList.size() > 0) {
            for (cartItem i : itemList) {
                if (i.getId() == id) {
                    i.setQuantity(i.getQuantity() + quantity);
                    return "Item quantity updated successfully.";
                }
            }
        }
        for (Product i : prodService.getList(null, null)) {
            if (i.getId().equals(id)) { // Had to do .equals() to compare values,else its some bullshit Integer objects
                                        // have a rule regarding,that beyond beyond 127 it compares references or sm
                                        // shi.Beyond 127 it creates new objects and compares references,whihc ofc isnt
                                        // equal
                itemList.add(new cartItem(i.getId(), i.getName(), i.getPrice(), quantity));
                return "New Item added to cart successfully.";
            }
        }
        return "Product not found.";
    }

    public String delCartItem(int id) {
        for (cartItem i : itemList) {
            if (i.getId().equals(id)) {
                itemList.remove(i);
                return "Item removed from cart successfully.";
            }
        }
        return "Item not found in cart.";
    }
}
