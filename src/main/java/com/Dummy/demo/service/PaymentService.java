package com.Dummy.demo.service;

import java.util.List;
import com.Dummy.demo.model.Order;
import java.util.ArrayList;
import com.Dummy.demo.model.Payment;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.Dummy.demo.service.internalSimulation.InternalErrorSimulator;
import com.Dummy.demo.service.internalSimulation.Context;

@Service
public class PaymentService {
    @Autowired
    private orderService orderService;
    @Autowired
    private InternalErrorSimulator internalErrorSimulator;
    private List<Payment> paymentList = new ArrayList<>();
    private int paymentIdCounter = 1;

    public List<Payment> getPayments() {
        Context ctx = new Context();
        ctx.service = "payment";
        ctx.operation = "PAYMENT_FETCH";
        internalErrorSimulator.inject(ctx.operation, ctx);
        return paymentList;
    }

    public String processPayment(int orderId, String method) {
        Context ctx = new Context();
        ctx.service = "payment";
        ctx.operation = "PAYMENT_PROCESS";
        internalErrorSimulator.inject(ctx.operation, ctx);
        System.out.println("Incoming orderId: " + orderId);

        Order target = null; // reference same

        for (Order o : orderService.getOrders()) {
            if (o.getOrderId() == orderId) {
                target = o;
                break;
            }
        }

        if (target == null)
            return "Order not found";

        if (target.getStatus().equals("PAID")) {
            return "Order already paid";
        }

        target.setStatus("PAID");

        paymentList.add(new Payment(paymentIdCounter, orderId, method, "SUCCESS"));
        paymentIdCounter++;

        return "Payment successful";
    }
}
