package com.Dummy.demo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Dummy.demo.DTO.PaymentRequest;
//import com.Dummy.demo.model.Payment;
import com.Dummy.demo.service.PaymentService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
   
    @PostMapping
    public String makePayment(@RequestBody PaymentRequest request) {
        return paymentService.processPayment(
            request.getOrderId(),
            request.getMethod()
        );
    }
}
