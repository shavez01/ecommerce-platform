package com.ecommerce.notification.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "order_notifications",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_order_notifications_order_id",
            columnNames = "order_id"
        )
    },
    indexes = {
        @Index(
            name = "idx_order_notifications_status",
            columnList = "status"
        )
    }
)
public class OrderNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false, length = 100)
    private String productId;

    @Column(name = "product_name")
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected OrderNotification() {
    }

    public static OrderNotification create(
            Long orderId,
            String productId,
            String productName,
            Integer quantity,
            BigDecimal totalPrice,
            LocalDateTime createdAt) {

        OrderNotification notification = new OrderNotification();

        notification.orderId = orderId;
        notification.productId = productId;
        notification.productName = productName;
        notification.quantity = quantity;
        notification.totalPrice = totalPrice;
        notification.status = "RECEIVED";
        notification.createdAt = createdAt;

        return notification;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}