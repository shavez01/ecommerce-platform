
package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.repository.OrderRepository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;



@Service
public class OrderService {

    private final RestClient productRestClient;
    private final RestClient inventoryRestClient;
    private final RestClient paymentRestClient;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderService(
            @Qualifier("productRestClient") RestClient productRestClient,
            @Qualifier("inventoryRestClient") RestClient inventoryRestClient,
            @Qualifier("paymentRestClient") RestClient paymentRestClient,
            OrderRepository orderRepository,
            OrderMapper orderMapper) {

        this.productRestClient = productRestClient;
        this.inventoryRestClient = inventoryRestClient;
        this.paymentRestClient = paymentRestClient;
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @SuppressWarnings("null")
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        // 1. Get product details
        Map<?, ?> product = productRestClient.get()
        .uri("/api/products/{id}", request.productId())
        .retrieve()
        .onStatus(
                status -> status.value() == 404,
                (req, res) -> {
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not available"
                    );
                }
        )
        .body(Map.class);

        BigDecimal price =
                new BigDecimal(product.get("price").toString());

        BigDecimal total = price.multiply(
                BigDecimal.valueOf(request.quantity()));

        // 2. Reserve inventory
        inventoryRestClient.post()
                .uri("/api/inventory/{id}/reserve?quantity={quantity}",
                        request.productId(), request.quantity())
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        (req, res) -> {
                        throw new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Inventory not available for this product"
                        );
                        }
                )
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
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        (req, res) -> {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_GATEWAY,
                                "Payment service is currently unable to process the payment"
                        );
                        }
                )
                .body(Map.class);

        if (payment == null ||
                !"SUCCESS".equals(payment.get("status"))) {

        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Payment failed: Transaction was not successful"
        );
        }

        // 5. Confirm order
        order.setStatus("CONFIRMED");
        order = orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    public OrderResponse getOrderById(@NonNull Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Order not found: " + id));

        return orderMapper.toResponse(order);
    }
}
