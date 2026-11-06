package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse processPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {

        return paymentService.processPayment(request, idempotencyKey);
    }

    
    @GetMapping("/{paymentId}")
    public PaymentResponse getPaymentByPaymentId(
            @PathVariable String paymentId) {

        return paymentService.getPaymentByPaymentId(paymentId);
    }

    @GetMapping("/order/{orderId}")
    public List<PaymentResponse> getPaymentsByOrderId(
            @PathVariable Long orderId) {

        return paymentService.getPaymentsByOrderId(orderId);
    }

}