package com.ecommerce.notification.repository;

import com.ecommerce.notification.entity.OrderNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderNotificationRepository
        extends JpaRepository<OrderNotification, Long> {

    boolean existsByOrderId(Long orderId);
}