package com.Dummy.demo.service;

import java.util.List;
import com.Dummy.demo.model.Order;
import java.util.ArrayList;
import com.Dummy.demo.model.Payment;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final orderService orderService;
    private List<Payment> paymentList = new ArrayList<>();
    private int paymentIdCounter = 1;

    public PaymentService(orderService orderService) {
        this.orderService = orderService;
    }

    public List<Payment> getPayments() {
        return paymentList;
    }

    public String processPayment(int orderId, String method) {
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
