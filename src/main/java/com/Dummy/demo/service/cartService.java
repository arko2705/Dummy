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
            if (i.getId() == id) {
                itemList.add(new cartItem(i.getId(), i.getName(), i.getPrice(), quantity));
                return "New Item added to cart successfully.";
            }
        }
        return "Product not found.";
    }
}
