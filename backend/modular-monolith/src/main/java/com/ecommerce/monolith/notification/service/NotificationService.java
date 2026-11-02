
package com.ecommerce.monolith.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;

import com.ecommerce.monolith.customer.service.CustomerService;
import com.ecommerce.monolith.kafka.event.OrderPlacedEvent;
import com.ecommerce.monolith.notification.entity.Notification;
import com.ecommerce.monolith.notification.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CustomerService customerService;

    public NotificationService(
            NotificationRepository notificationRepository,
            CustomerService customerService) {
        this.notificationRepository = notificationRepository;
        this.customerService = customerService;
    }

    // Record notification
    public Notification recordNotification(
        Long orderId,
        String notificationType,
        String recipientEmail,
        String subject,
        String message) {

        // Return existing notification if this event was already processed
        return notificationRepository
                .findByOrderIdAndNotificationType(orderId, notificationType)
                .orElseGet(() -> {

                    Notification notification = new Notification();

                    notification.setOrderId(orderId);
                    notification.setNotificationType(notificationType);
                    notification.setRecipientEmail(recipientEmail);
                    notification.setSubject(subject);
                    notification.setMessage(message);
                    notification.setDeliveryStatus("SIMULATED");
                    notification.setCreatedAt(LocalDateTime.now());

                    try {
                        return notificationRepository.saveAndFlush(notification);

                    } catch (DataIntegrityViolationException exception) {

                        // Another consumer may have inserted the same notification
                        return notificationRepository
                                .findByOrderIdAndNotificationType(
                                        orderId, notificationType)
                                .orElseThrow(() -> exception);
                    }
                });
    }

    // Get notification by ID
    public Notification getNotificationById(Long id) {

        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notification not found with ID: " + id));
    }

    // Get all notifications
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    // Get notifications by Order ID
    public List<Notification> getNotificationsByOrderId(Long orderId) {

        return notificationRepository
                .findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    // Process Order Placed Kafka Event
    public void processOrderPlacedEvent(OrderPlacedEvent event) {

        // Retrieve actual customer email
        String recipientEmail =
                customerService.getCustomerEmailById(event.customerId());

        String subject =
                "Order Confirmation - #" + event.orderId();

        String message = "Your order has been successfully placed. "
                + "Order ID: " + event.orderId()
                + ", Total Amount: " + event.totalAmount()
                + ", Order Date: " + event.orderDate();

        recordNotification(
                event.orderId(),
                "ORDER_CONFIRMATION",
                recipientEmail,
                subject,
                message
        );
    }
}
