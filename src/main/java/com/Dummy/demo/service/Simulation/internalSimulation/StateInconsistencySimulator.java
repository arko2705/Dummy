package com.Dummy.demo.service.Simulation.internalSimulation;

import java.util.Random;

public class StateInconsistencySimulator implements FailureStrategy {

    private Random random = new Random();

    @Override
    public boolean shouldTrigger(Context ctx) {
        return random.nextInt(100) < 30; // 30% chance
    }

    @Override
    public void execute(Context ctx) {

        String op = ctx.operation;
        int type = random.nextInt(3);
        System.out.println("Current operation " + op + " with type " + type);
        switch (op) {

            // 🛍️ PRODUCT
            case "PRODUCT_FETCH":// will change this logic later in other versions
                if (type == 0)
                    ctx.data.put("missingProducts", true);
                if (type == 1)
                    ctx.data.put("duplicateProducts", true);
                if (type == 2)
                    ctx.data.put("staleProductData", true);
                break;

            case "PRODUCT_UPDATE":
                if (type == 0)
                    ctx.data.put("updateNotApplied", true);
                if (type == 1)
                    ctx.data.put("partialUpdate", true);
                if (type == 2)
                    ctx.data.put("wrongPriceUpdate", true);
                break;

            // 🛒 CART
            case "CART_ADD":
                if (type == 1)
                    ctx.data.put("productDeleted", true);
                if (type == 2)
                    ctx.data.put("wrongQuantity", true);
                break;

            case "CART_FETCH":
                if (type == 0)
                    ctx.data.put("missingCartItems", true);
                if (type == 1)
                    ctx.data.put("duplicateCartItems", true);

                break;

            // 📦 ORDER
            case "ORDER_CREATE":
                if (type == 1)// i gotta fix this logic later,just for V1
                    ctx.data.put("emptyOrder", true);
                break;

            case "ORDER_FETCH":
                if (type == 0)
                    ctx.data.put("missingOrders", true);
                if (type == 1)
                    ctx.data.put("duplicateOrders", true);
                if (type == 2)
                    ctx.data.put("wrongOrderStatus", true);
                break;

            // 💳 PAYMENT
            case "PAYMENT_PROCESS":
                if (type == 0)
                    ctx.data.put("paymentSuccessButNotSaved", true);
                if (type == 1)
                    ctx.data.put("doublePayment", true);
                if (type == 2)
                    ctx.data.put("statusMismatch", true);
                break;

            case "PAYMENT_FETCH":
                if (type == 0)
                    ctx.data.put("missingPayments", true);
                if (type == 1)
                    ctx.data.put("duplicatePayments", true);
                if (type == 2)
                    ctx.data.put("wrongPaymentStatus", true);
                break;
        }
    }
}