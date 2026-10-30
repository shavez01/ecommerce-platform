package com.ecommerce.monolith.payment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.monolith.customer.entity.Customer;
import com.ecommerce.monolith.customer.service.CustomerService;
import com.ecommerce.monolith.order.entity.Order;
import com.ecommerce.monolith.order.service.OrderService;
import com.ecommerce.monolith.notification.service.NotificationService;
import com.ecommerce.monolith.payment.dto.PaymentRequest;
import com.ecommerce.monolith.payment.entity.Payment;
import com.ecommerce.monolith.payment.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final CustomerService customerService;
    private final NotificationService notificationService;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderService orderService,
            CustomerService customerService,
            NotificationService notificationService) {

        this.paymentRepository = paymentRepository;
        this.orderService = orderService;
        this.customerService = customerService;
        this.notificationService = notificationService;
    }

    // Process payment
    @Transactional
    public Payment processPayment(PaymentRequest request) {

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
        payment.setStatus("SUCCESS");
        payment.setTransactionReference(
                "TXN-" + UUID.randomUUID());
        payment.setCreatedAt(LocalDateTime.now());

        // Save payment
        Payment savedPayment = paymentRepository.save(payment);

        // Update order status
        orderService.markOrderAsPaid(order.getId());

        Customer customer =
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
