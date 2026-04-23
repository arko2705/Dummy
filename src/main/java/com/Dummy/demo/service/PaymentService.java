package com.Dummy.demo.service;

import java.util.List;
import com.Dummy.demo.model.Order;
import java.util.ArrayList;
import com.Dummy.demo.model.Payment;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import com.Dummy.demo.monitoring.service.RequestMetricsService;
import com.Dummy.demo.service.Simulation.internalSimulation.Context;
import com.Dummy.demo.service.Simulation.internalSimulation.InternalErrorSimulator;
import com.Dummy.demo.service.externalDependency.client.fakeDBClient;
import com.Dummy.demo.service.externalDependency.client.paymentGatewayClient;
import com.Dummy.demo.service.externalDependency.client.thirdPartyAPIClient;
import com.Dummy.demo.service.externalDependency.util.ReqBuilder;

@Service
public class PaymentService {
    @Autowired
    private orderService orderService;
    @Autowired
    private RequestMetricsService metricsService;
    @Autowired
    private fakeDBClient dbClient;
    @Autowired
    private paymentGatewayClient paymentGatewayClient;
    @Autowired
    private thirdPartyAPIClient apiClient;
    @Autowired
    private InternalErrorSimulator internalErrorSimulator;
    private List<Payment> paymentList = new ArrayList<>();
    private int paymentIdCounter = 1;
    private ReqBuilder reqBuilder;

    public PaymentService(ReqBuilder reqBuilder) {
        this.reqBuilder = reqBuilder;
    }

    public List<Payment> getPayments() {
        try {
            dbClient.fetchData(reqBuilder.buildDepRequest("DB", "PAYMENT_FETCH"));
        } catch (Exception e) {
            System.out.println("DB Client Error: " + e.getMessage());
            throw e;
        }
        Context ctx = new Context();
        ctx.service = "payment";
        ctx.operation = "PAYMENT_FETCH";
        try {
            internalErrorSimulator.inject(ctx.operation, ctx);
        } catch (RuntimeException e) {
            // ⚠️ OPERATION FAILED
            throw e;
        }
        return paymentList;
    }

    public String processPayment(int orderId, String method) {
        try {
            dbClient.fetchData(reqBuilder.buildDepRequest("DB", "PAYMENT_PRECHECK"));
        } catch (Exception e) {
            System.out.println("DB Client Error: " + e.getMessage());
            throw e;
        }
        try {
            paymentGatewayClient.processPayment(reqBuilder.buildDepRequest("PAYMENT", "PAYMENT_PROCESS"));
        } catch (Exception e) {
            System.out.println("Payment Gateway Client Error: " + e.getMessage());
            throw e;
        }
        try {
            apiClient.callAPI(reqBuilder.buildDepRequest("API", "FRAUD_CHECK"));
        } catch (Exception e) {
            System.out.println("API Client Error:Failed to do Fraud check " + e.getMessage());
            throw e;
        }
        Context ctx = new Context();
        ctx.service = "payment";
        ctx.operation = "PAYMENT_PROCESS";
        ctx.data = new HashMap<>();
        try {
            internalErrorSimulator.inject(ctx.operation, ctx);
            System.out.println("Incoming orderId: " + orderId);

            Order target = null; // reference same

            for (Order o : orderService.getOrders()) {
                if (o.getOrderId() == orderId) {
                    target = o;
                    break;
                }
            }

            if (target == null)// can create error out of this,order present but showing order not found
                return "Order not found";
            if (ctx.data.get("paymentSuccessButNotSaved") != null) {// stavya animesh buddy do your thing with the
                                                                    // logging
                                                                    // logic
                target.setStatus("PAID");
                return "Payment successful"; // but not stored
            }

            if (ctx.data.get("doublePayment") != null) {
                paymentList.add(new Payment(paymentIdCounter, orderId, method, "SUCCESS"));
            }

            if (target.getStatus().equals("PAID")) {
                return "Order already paid";
            }

            target.setStatus("PAID");
            if (ctx.data.get("statusMismatch") != null) {
                System.out.println("Status Mismatch Simulated!");
                target.setStatus("FAILED");
            }
            paymentList.add(new Payment(paymentIdCounter, orderId, method, "SUCCESS"));
            paymentIdCounter++;

            return "Payment successful";
        } catch (RuntimeException e) {
            // Operation failed
            throw e;
        }
    }
}
