
package com.ecommerce.monolith.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.monolith.notification.entity.Notification;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
