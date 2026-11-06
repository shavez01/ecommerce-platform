package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.enums.PaymentStatus;
import com.ecommerce.payment.gateway.GatewayPaymentResult;
import com.ecommerce.payment.gateway.PaymentGatewaySimulator;
import com.ecommerce.payment.repository.PaymentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewaySimulator paymentGatewaySimulator;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentGatewaySimulator paymentGatewaySimulator) {

        this.paymentRepository = paymentRepository;
        this.paymentGatewaySimulator = paymentGatewaySimulator;
    }

    @Transactional
    public PaymentResponse processPayment(
            PaymentRequest request,
            String idempotencyKey) {

        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || idempotencyKey.length() > 100) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid Idempotency-Key header is required"
            );
        }

        // 1. Check whether this request was already processed
        var existingPayment = paymentRepository
                .findByIdempotencyKey(idempotencyKey);

        if (existingPayment.isPresent()) {

            Payment payment = existingPayment.get();

            // Prevent reuse of the same key for a different request
            boolean sameRequest =
                    payment.getOrderId().equals(request.orderId())
                    && payment.getAmount().compareTo(request.amount()) == 0
                    && payment.getPaymentMethod().equalsIgnoreCase(request.method());

            if (!sameRequest) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Idempotency key was already used for a different payment request"
                );
            }

            return mapToResponse(payment);
        }

        // 2. Create a new payment
        LocalDateTime now = LocalDateTime.now();

        Payment payment = Payment.create(
                UUID.randomUUID().toString(),
                request.orderId(),
                request.amount(),
                "INR",
                request.method(),
                idempotencyKey,
                now
        );

        Payment savedPayment = paymentRepository.save(payment);

        // 3. Call simulated gateway
        GatewayPaymentResult gatewayResult =
                paymentGatewaySimulator.authorize();

        // 4. Update payment status
        if (gatewayResult.approved()) {

            savedPayment.completePayment(
                    PaymentStatus.SUCCESS,
                    gatewayResult.transactionReference(),
                    null
            );

        } else {

            savedPayment.completePayment(
                    PaymentStatus.FAILED,
                    null,
                    gatewayResult.failureReason()
            );
        }

        return mapToResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByPaymentId(String paymentId) {

        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Payment not found: " + paymentId
                ));

        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {

        return paymentRepository.findByOrderId(orderId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private PaymentResponse mapToResponse(Payment payment) {

        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus().name()
        );
    }
}
