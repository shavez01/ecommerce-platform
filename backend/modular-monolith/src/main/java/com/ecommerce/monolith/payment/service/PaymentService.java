package com.ecommerce.monolith.payment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.monolith.common.outbox.service.OutboxService;
import com.ecommerce.monolith.customer.dto.CustomerResponse;
import com.ecommerce.monolith.customer.service.CustomerService;
import com.ecommerce.monolith.kafka.config.KafkaTopicConfig;
import com.ecommerce.monolith.kafka.event.PaymentSuccessEvent;
import com.ecommerce.monolith.kafka.event.PaymentFailedEvent;
import com.ecommerce.monolith.kafka.producer.PaymentEventProducer;
import com.ecommerce.monolith.order.entity.Order;
import com.ecommerce.monolith.order.service.OrderService;
import com.ecommerce.monolith.notification.service.NotificationService;
import com.ecommerce.monolith.payment.dto.PaymentRequest;
import com.ecommerce.monolith.payment.entity.Payment;
import com.ecommerce.monolith.payment.gateway.PaymentGateway;
import com.ecommerce.monolith.payment.repository.PaymentRepository;



@Service
public class PaymentService {

    private static final Logger log =
        LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final CustomerService customerService;
    private final NotificationService notificationService;
    private final PaymentGateway paymentGateway;
    private final OutboxService outboxService;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderService orderService,
            CustomerService customerService,
            NotificationService notificationService,
            PaymentEventProducer paymentEventProducer,
            PaymentGateway paymentGateway,
            OutboxService outboxService) {

        this.paymentRepository = paymentRepository;
        this.orderService = orderService;
        this.customerService = customerService;
        this.notificationService = notificationService;
        this.paymentGateway = paymentGateway;
        this.outboxService = outboxService;
    }

    // Process payment
    @Transactional
    public Payment processPayment(PaymentRequest request, String idempotencyKey) {

        log.info("Received Idempotency-Key: {}", idempotencyKey);

        // 1. Validate Idempotency-Key
        if (idempotencyKey == null || idempotencyKey.isBlank()
                || idempotencyKey.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid Idempotency-Key");
        }

        // 2. Check for an existing payment
        Optional<Payment> existingPayment =
                paymentRepository.findByIdempotencyKey(idempotencyKey);

        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();

            if (!payment.getOrderId().equals(request.getOrderId())
                    || !payment.getPaymentMethod().equals(request.getPaymentMethod())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Idempotency key already used with different payment details");
            }

            return payment;
        }

        if (request.getOrderId() == null
                || request.getPaymentMethod() == null
                || request.getPaymentMethod().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order ID and payment method are required");
        }

        // Prevent duplicate payments
        if (paymentRepository.existsByOrderId(request.getOrderId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment already exists for this order");
        }

        // Retrieve order from Order module
        Order order = orderService.getOrderById(request.getOrderId());

        if (!"PLACED".equals(order.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order is not eligible for payment");
        }

        // Simulate successful payment processing
        Payment payment = new Payment();

        payment.setOrderId(order.getId());
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setIdempotencyKey(idempotencyKey);

        payment.setTransactionReference(
                "TXN-" + UUID.randomUUID());

        payment.setCreatedAt(LocalDateTime.now());

        // Simulate payment failure when requested
        if (request.isSimulateFailure()) {

            payment.setStatus("FAILED");

            Payment savedPayment = paymentRepository.save(payment);

            PaymentFailedEvent event = new PaymentFailedEvent(
                    savedPayment.getId(),
                    savedPayment.getOrderId(),
                    savedPayment.getAmount(),
                    savedPayment.getPaymentMethod(),
                    savedPayment.getTransactionReference(),
                    "Simulated payment failure",
                    savedPayment.getCreatedAt()
            );

            outboxService.saveEvent(
                "PAYMENT_FAILED",
                String.valueOf(savedPayment.getOrderId()),
                KafkaTopicConfig.PAYMENT_FAILED_TOPIC,
                event
            );

            return savedPayment;
        }

        // Process payment through the gateway
        boolean gatewayApproved = paymentGateway.charge(
                order.getId(),
                order.getTotalAmount(),
                request.getPaymentMethod()
        );

        if (!gatewayApproved) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Payment gateway is currently unavailable. Please try again later."
            );
        }

        // Payment approved
        payment.setStatus("SUCCESS");

        Payment savedPayment = paymentRepository.save(payment);

        orderService.markOrderAsPaid(order.getId());

        PaymentSuccessEvent event = new PaymentSuccessEvent(
                savedPayment.getId(),
                savedPayment.getOrderId(),
                savedPayment.getAmount(),
                savedPayment.getPaymentMethod(),
                savedPayment.getTransactionReference(),
                savedPayment.getCreatedAt()
        );

        outboxService.saveEvent(
            "PAYMENT_SUCCESS",
            String.valueOf(savedPayment.getOrderId()),
            KafkaTopicConfig.PAYMENT_SUCCESS_TOPIC,
            event
        );

        CustomerResponse customer =
                customerService.getCustomerById(order.getCustomerId());

        notificationService.recordNotification(
                order.getId(),
                "PAYMENT_SUCCESS",
                customer.getEmail(),
                "Payment Successful",
                "Payment for order #" + order.getId()
                        + " was successful.");

        return savedPayment;
    }

    // Get payment by ID
    public Payment getPaymentById(Long id) {

        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Payment not found with ID: " + id));
    }

    // Get payment by Order ID
    public Payment getPaymentByOrderId(Long orderId) {

        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Payment not found for order ID: " + orderId));
    }

    // Get all payments
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}
