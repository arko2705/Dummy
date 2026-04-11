package com.Dummy.demo.model;

//To instantiate child class,we need to instantiate parent class too first,hence we send parameters using super
public class cartItem extends Product {
    private int quantity;

    public cartItem(Integer id, String name, Double price, int quantity) { // a cart object with empty name and price
                                                                           // made for the request body
        super(id, name, price);
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}
// We make a cartItem with just quantity,