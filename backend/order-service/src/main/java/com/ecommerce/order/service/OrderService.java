package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.repository.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;
// import java.util.UUID;

@Service
public class OrderService {

    private final RestClient productRestClient;
    private final RestClient inventoryRestClient;
    private final RestClient paymentRestClient;
    private final OrderRepository orderRepository;

    public OrderService(
            RestClient productRestClient,
            RestClient inventoryRestClient,
            RestClient paymentRestClient,
            OrderRepository orderRepository) {

        this.productRestClient = productRestClient;
        this.inventoryRestClient = inventoryRestClient;
        this.paymentRestClient = paymentRestClient;
        this.orderRepository = orderRepository;
    }

@SuppressWarnings("null")
@Transactional
    public OrderResponse createOrder(OrderRequest request) {

        // 1. Get product details
        Map<?, ?> product = productRestClient.get()
                .uri("/api/products/{id}", request.productId())
                .retrieve()
                .body(Map.class);

        if (product == null) {
            throw new IllegalStateException("Product not found");
        }

        BigDecimal price =
                new BigDecimal(product.get("price").toString());

        BigDecimal total = price.multiply(
                BigDecimal.valueOf(request.quantity()));

        // 2. Reserve inventory
        inventoryRestClient.post()
                .uri("/api/inventory/{id}/reserve?quantity={quantity}",
                        request.productId(), request.quantity())
                .retrieve()
                .toBodilessEntity();

        // 3. Save pending order
        Order order = new Order();
        order.setProductId(request.productId());
        order.setProductName(product.get("name").toString());
        order.setQuantity(request.quantity());
        order.setTotalPrice(total);
        order.setStatus("PENDING");

        order = orderRepository.save(order);

        // 4. Process payment
        Map<?, ?> payment = paymentRestClient.post()
                .uri("/api/payments")
                .body(Map.of(
                        "orderId", order.getId().toString(),
                        "amount", total,
                        "method", "CARD"
                ))
                .retrieve()
                .body(Map.class);

        if (payment == null ||
                !"SUCCESS".equals(payment.get("status"))) {
            throw new IllegalStateException("Payment failed");
        }

        // 5. Confirm order
        order.setStatus("CONFIRMED");
        order = orderRepository.save(order);

        return new OrderResponse(
                order.getId(),
                order.getProductId(),
                order.getProductName(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getStatus()
        );
    }

    public java.util.List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new java.util.NoSuchElementException(
                                "Order not found: " + id));
    }
}

// @Service
// public class OrderService {

//     private final RestClient productRestClient;
//     private final RestClient inventoryRestClient;
//     private final RestClient paymentRestClient;

//     public OrderService(
//             RestClient productRestClient,
//             RestClient inventoryRestClient,
//             RestClient paymentRestClient) {
//         this.productRestClient = productRestClient;
//         this.inventoryRestClient = inventoryRestClient;
//         this.paymentRestClient = paymentRestClient;
//     }

//     public OrderResponse createOrder(OrderRequest request) {
//         // 1. Retrieve product
//         Map<?, ?> product = productRestClient.get()
//                 .uri("/api/products/{id}", request.productId())
//                 .retrieve()
//                 .body(Map.class);

//         if (product == null) {
//             throw new IllegalStateException("Empty product response");
//         }

//         BigDecimal price =
//                 new BigDecimal(product.get("price").toString());

//         BigDecimal total = price.multiply(
//                 BigDecimal.valueOf(request.quantity()));

//         // 2. Reserve inventory
//         inventoryRestClient.post()
//                 .uri("/api/inventory/{id}/reserve?quantity={quantity}",
//                         request.productId(), request.quantity())
//                 .retrieve()
//                 .toBodilessEntity();

//         // 3. Process simulated payment
//         String orderId = UUID.randomUUID().toString();

//         Map<?, ?> payment = paymentRestClient.post()
//                 .uri("/api/payments")
//                 .body(Map.of(
//                         "orderId", orderId,
//                         "amount", total,
//                         "method", "CARD"
//                 ))
//                 .retrieve()
//                 .body(Map.class);

//         if (payment == null
//                 || !"SUCCESS".equals(payment.get("status"))) {
//             throw new IllegalStateException("Payment failed");
//         }

//         // 4. Return confirmed order
//         return new OrderResponse(
//                 request.productId(),
//                 product.get("name").toString(),
//                 request.quantity(),
//                 total,
//                 "CONFIRMED"
//         );
//     }
// }