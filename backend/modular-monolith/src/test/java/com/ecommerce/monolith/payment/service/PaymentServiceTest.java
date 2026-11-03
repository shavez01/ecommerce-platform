package com.ecommerce.monolith.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.monolith.common.outbox.service.OutboxService;
import com.ecommerce.monolith.customer.dto.CustomerResponse;
import com.ecommerce.monolith.customer.service.CustomerService;
import com.ecommerce.monolith.kafka.producer.PaymentEventProducer;
import com.ecommerce.monolith.notification.service.NotificationService;
import com.ecommerce.monolith.order.entity.Order;
import com.ecommerce.monolith.order.service.OrderService;
import com.ecommerce.monolith.payment.dto.PaymentRequest;
import com.ecommerce.monolith.payment.entity.Payment;
import com.ecommerce.monolith.payment.gateway.PaymentGateway;
import com.ecommerce.monolith.payment.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock PaymentRepository paymentRepository;
    @Mock OrderService orderService;
    @Mock CustomerService customerService;
    @Mock NotificationService notificationService;
    @Mock PaymentEventProducer paymentEventProducer;
    @Mock PaymentGateway paymentGateway;
    @Mock OutboxService outboxService;

    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(paymentRepository, orderService, customerService,
                notificationService, paymentEventProducer, paymentGateway, outboxService);
    }

    private PaymentRequest request(Long orderId, String method, boolean failure) {
        PaymentRequest r = new PaymentRequest();
        ReflectionTestUtils.setField(r, "orderId", orderId);
        ReflectionTestUtils.setField(r, "paymentMethod", method);
        ReflectionTestUtils.setField(r, "simulateFailure", failure);
        return r;
    }

    private Order placedOrder() {
        Order o = new Order();
        o.setId(50L);
        o.setCustomerId(10L);
        o.setTotalAmount(new BigDecimal("125.00"));
        o.setStatus("PLACED");
        return o;
    }

    @Test
    void successfulPaymentSavesPaymentMarksOrderPaidAndWritesOutbox() {
        Order order = placedOrder();
        when(paymentRepository.findByIdempotencyKey("pay-key")).thenReturn(Optional.empty());
        when(paymentRepository.existsByOrderId(50L)).thenReturn(false);
        when(orderService.getOrderById(50L)).thenReturn(order);
        when(paymentGateway.charge(50L, order.getTotalAmount(), "CARD")).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(200L);
            return p;
        });
        CustomerResponse customer = new CustomerResponse();
        ReflectionTestUtils.setField(customer, "email", "test@example.com");
        when(customerService.getCustomerById(10L)).thenReturn(customer);

        Payment result = service.processPayment(request(50L, "CARD", false), "pay-key");

        assertEquals(200L, result.getId());
        assertEquals("SUCCESS", result.getStatus());
        verify(orderService).markOrderAsPaid(50L);
        verify(outboxService).saveEvent(eq("PAYMENT_SUCCESS"), eq("50"), anyString(), any());
        verify(notificationService).recordNotification(eq(50L), eq("PAYMENT_SUCCESS"),
                eq("test@example.com"), anyString(), anyString());
    }

    @Test
    void simulatedFailureWritesFailedPaymentAndOutbox() {
        Order order = placedOrder();
        when(paymentRepository.findByIdempotencyKey("pay-key")).thenReturn(Optional.empty());
        when(paymentRepository.existsByOrderId(50L)).thenReturn(false);
        when(orderService.getOrderById(50L)).thenReturn(order);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(201L);
            return p;
        });

        Payment result = service.processPayment(request(50L, "CARD", true), "pay-key");

        assertEquals("FAILED", result.getStatus());
        verify(outboxService).saveEvent(eq("PAYMENT_FAILED"), eq("50"), anyString(), any());
        verifyNoInteractions(paymentGateway);
        verify(orderService, never()).markOrderAsPaid(anyLong());
    }

    @Test
    void gatewayDeclineReturnsBadGateway() {
        Order order = placedOrder();
        when(paymentRepository.findByIdempotencyKey("pay-key")).thenReturn(Optional.empty());
        when(paymentRepository.existsByOrderId(50L)).thenReturn(false);
        when(orderService.getOrderById(50L)).thenReturn(order);
        when(paymentGateway.charge(50L, order.getTotalAmount(), "CARD")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.processPayment(request(50L, "CARD", false), "pay-key"));
        assertEquals(502, ex.getStatusCode().value());
        verify(paymentRepository, never()).save(any());
        verifyNoInteractions(outboxService);
    }

    @Test
    void invalidIdempotencyKeyReturnsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.processPayment(request(50L, "CARD", false), " "));
        assertEquals(400, ex.getStatusCode().value());
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void existingPaymentWithSameKeyIsReturned() {
        Payment existing = new Payment();
        existing.setOrderId(50L);
        existing.setPaymentMethod("CARD");
        when(paymentRepository.findByIdempotencyKey("pay-key")).thenReturn(Optional.of(existing));

        assertSame(existing, service.processPayment(request(50L, "CARD", false), "pay-key"));
        verifyNoInteractions(orderService, paymentGateway, outboxService);
    }
}
