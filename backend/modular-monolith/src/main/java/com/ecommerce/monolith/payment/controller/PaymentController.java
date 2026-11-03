
package com.ecommerce.monolith.payment.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.monolith.payment.dto.PaymentRequest;
import com.ecommerce.monolith.payment.entity.Payment;
import com.ecommerce.monolith.payment.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Process payment
    @PostMapping
    public ResponseEntity<Payment> processPayment(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        Payment payment =
                paymentService.processPayment(request, idempotencyKey);

        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    // Get payment by ID
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                paymentService.getPaymentById(id));
    }

    // Get payment by Order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrderId(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                paymentService.getPaymentByOrderId(orderId));
    }

    // Get all payments
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {

        return ResponseEntity.ok(
                paymentService.getAllPayments());
    }

    // Health check
    @GetMapping("/health")
    public String health() {
        return "Payment module is running";
    }
}
