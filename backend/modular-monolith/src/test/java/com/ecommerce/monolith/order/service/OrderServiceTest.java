package com.ecommerce.monolith.order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.monolith.common.outbox.service.OutboxService;
import com.ecommerce.monolith.customer.service.CustomerService;
import com.ecommerce.monolith.inventory.service.InventoryService;
import com.ecommerce.monolith.kafka.producer.OrderEventProducer;
import com.ecommerce.monolith.notification.service.NotificationService;
import com.ecommerce.monolith.order.dto.OrderRequest;
import com.ecommerce.monolith.order.entity.Order;
import com.ecommerce.monolith.order.repository.OrderRepository;
import com.ecommerce.monolith.product.entity.Product;
import com.ecommerce.monolith.product.service.ProductService;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock CustomerService customerService;
    @Mock ProductService productService;
    @Mock InventoryService inventoryService;
    @Mock OrderEventProducer orderEventProducer;
    @Mock NotificationService notificationService;
    @Mock OutboxService outboxService;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(orderRepository, customerService, productService,
                inventoryService, orderEventProducer, notificationService, outboxService);
    }

    private OrderRequest request(Long customerId, Long productId, Integer quantity) {
        OrderRequest r = new OrderRequest();
        ReflectionTestUtils.setField(r, "customerId", customerId);
        ReflectionTestUtils.setField(r, "productId", productId);
        ReflectionTestUtils.setField(r, "quantity", quantity);
        return r;
    }

    private Product product(boolean active, BigDecimal price) {
        Product p = new Product();
        ReflectionTestUtils.setField(p, "active", active);
        ReflectionTestUtils.setField(p, "price", price);
        return p;
    }

    @Test
    void placeOrderCreatesOrderAndOutboxEvent() {
        OrderRequest request = request(10L, 20L, 3);
        when(orderRepository.findByIdempotencyKey("order-key")).thenReturn(Optional.empty());
        when(productService.getProductById(20L)).thenReturn(product(true, new BigDecimal("25.00")));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            return o;
        });

        Order result = service.placeOrder(request, "order-key");

        assertEquals(100L, result.getId());
        assertEquals(new BigDecimal("75.00"), result.getTotalAmount());
        assertEquals("PLACED", result.getStatus());
        assertEquals("order-key", result.getIdempotencyKey());
        verify(inventoryService).reduceStock(20L, 3);
        verify(outboxService).saveEvent(eq("ORDER_PLACED"), eq("100"),
                anyString(), any());
    }

    @Test
    void sameIdempotencyKeyReturnsExistingOrderWithoutChangingStock() {
        Order existing = new Order();
        existing.setCustomerId(10L);
        existing.setProductId(20L);
        existing.setQuantity(3);
        when(orderRepository.findByIdempotencyKey("order-key")).thenReturn(Optional.of(existing));

        assertSame(existing, service.placeOrder(request(10L, 20L, 3), "order-key"));
        verifyNoInteractions(productService, inventoryService, outboxService);
    }

    @Test
    void reusingKeyWithDifferentPayloadReturnsConflict() {
        Order existing = new Order();
        existing.setCustomerId(10L);
        existing.setProductId(20L);
        existing.setQuantity(3);
        when(orderRepository.findByIdempotencyKey("order-key")).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class,
                () -> service.placeOrder(request(10L, 20L, 4), "order-key"));
        verifyNoInteractions(productService, inventoryService, outboxService);
    }

    @Test
    void invalidIdempotencyKeyReturnsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.placeOrder(request(10L, 20L, 1), " "));
        assertEquals(400, ex.getStatusCode().value());
        verifyNoInteractions(orderRepository);
    }

    @Test
    void inactiveProductReturnsConflict() {
        when(orderRepository.findByIdempotencyKey("order-key")).thenReturn(Optional.empty());
        when(productService.getProductById(20L)).thenReturn(product(false, new BigDecimal("25")));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.placeOrder(request(10L, 20L, 1), "order-key"));
        assertEquals(409, ex.getStatusCode().value());
        verifyNoInteractions(inventoryService, outboxService);
    }

    @Test
    void markOrderAsPaidUpdatesStatus() {
        Order order = new Order();
        order.setStatus("PLACED");
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        assertEquals("PAID", service.markOrderAsPaid(5L).getStatus());
    }
}
