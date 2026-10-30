
package com.ecommerce.monolith.order.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.monolith.order.dto.OrderRequest;
import com.ecommerce.monolith.order.entity.Order;
import com.ecommerce.monolith.order.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Place order
    @PostMapping
    public ResponseEntity<Order> placeOrder(
            @RequestBody OrderRequest request) {

        Order order = orderService.placeOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(order);
    }

    // Get order by ID
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                orderService.getOrderById(id));
    }

    // Get all orders
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {

        return ResponseEntity.ok(
                orderService.getAllOrders());
    }

    // Health check
    @GetMapping("/health")
    public String health() {
        return "Order module is running";
    }
}
