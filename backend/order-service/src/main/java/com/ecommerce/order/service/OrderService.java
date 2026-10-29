package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class OrderService {

    private final RestClient inventoryRestClient;
    private final RestClient productRestClient;


    public OrderService(
            RestClient productRestClient,
            RestClient inventoryRestClient) {
        this.productRestClient = productRestClient;
        this.inventoryRestClient = inventoryRestClient;
    }

    public OrderResponse createOrder(OrderRequest request) {
        Map<?, ?> product;

        try {
            product = productRestClient.get()
                    .uri("/api/products/{id}", request.productId())
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientResponseException ex) {
            throw new IllegalArgumentException(
                    "Product service rejected the request: "
                            + ex.getStatusCode());
        }

        if (product == null) {
            throw new IllegalStateException("Empty product response");
        }

        BigDecimal price = new BigDecimal(product.get("price").toString());
        BigDecimal total = price.multiply(
                BigDecimal.valueOf(request.quantity()));

        inventoryRestClient.post()
        .uri("/api/inventory/{id}/reserve?quantity={quantity}",
                request.productId(), request.quantity())
        .retrieve()
        .toBodilessEntity();

        return new OrderResponse(
            request.productId(),
            product.get("name").toString(),
            request.quantity(),
            total,
            "CREATED" 
        );
    }
}