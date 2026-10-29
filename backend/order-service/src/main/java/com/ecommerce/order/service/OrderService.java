package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private final RestClient productRestClient;
    private final RestClient inventoryRestClient;
    private final RestClient paymentRestClient;

    public OrderService(
            RestClient productRestClient,
            RestClient inventoryRestClient,
            RestClient paymentRestClient) {
        this.productRestClient = productRestClient;
        this.inventoryRestClient = inventoryRestClient;
        this.paymentRestClient = paymentRestClient;
    }

    public OrderResponse createOrder(OrderRequest request) {
        // 1. Retrieve product
        Map<?, ?> product = productRestClient.get()
                .uri("/api/products/{id}", request.productId())
                .retrieve()
                .body(Map.class);

        if (product == null) {
            throw new IllegalStateException("Empty product response");
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

        // 3. Process simulated payment
        String orderId = UUID.randomUUID().toString();

        Map<?, ?> payment = paymentRestClient.post()
                .uri("/api/payments")
                .body(Map.of(
                        "orderId", orderId,
                        "amount", total,
                        "method", "CARD"
                ))
                .retrieve()
                .body(Map.class);

        if (payment == null
                || !"SUCCESS".equals(payment.get("status"))) {
            throw new IllegalStateException("Payment failed");
        }

        // 4. Return confirmed order
        return new OrderResponse(
                request.productId(),
                product.get("name").toString(),
                request.quantity(),
                total,
                "CONFIRMED"
        );
    }
}