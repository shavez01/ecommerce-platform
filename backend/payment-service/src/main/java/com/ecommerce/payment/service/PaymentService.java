package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    public PaymentResponse processPayment(PaymentRequest request) {
        return new PaymentResponse(
                UUID.randomUUID().toString(),
                request.orderId(),
                request.amount(),
                "SUCCESS"
        );
    }
}