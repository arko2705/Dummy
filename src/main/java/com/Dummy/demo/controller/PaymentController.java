package com.Dummy.demo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import com.Dummy.demo.model.PaymentRequest;
//import com.Dummy.demo.model.Payment;
import com.Dummy.demo.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;

//import io.swagger.v3.oas.annotations.parameters.RequestBody;   buddy delete this import
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import com.Dummy.demo.model.Payment;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public List<Payment> getPayments(HttpServletRequest request) {
        return paymentService.getPayments(request);
    }

    @PostMapping("/make")
    public String makePayment(@RequestBody PaymentRequest request) {
        return paymentService.processPayment(
                request.getOrderId(),
                request.getMethod());
    }

}
