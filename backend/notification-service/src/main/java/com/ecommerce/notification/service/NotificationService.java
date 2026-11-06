package com.ecommerce.notification.service;

import com.ecommerce.notification.dto.NotificationRequest;
import com.ecommerce.notification.entity.Notification;
import com.ecommerce.notification.repository.NotificationRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // Create notification
    @Transactional
    public Map<String, String> send(NotificationRequest request) {

        String notificationId = UUID.randomUUID().toString();

        Notification notification = Notification.create(
                notificationId,
                request.recipient(),
                request.subject(),
                request.message(),
                LocalDateTime.now()
        );

        Notification savedNotification =
                notificationRepository.save(notification);

        return mapToResponse(savedNotification);
    }

    // Get notification by UUID
    @Transactional(readOnly = true)
    public Map<String, String> getByNotificationId(String notificationId) {

        Notification notification = notificationRepository
                .findByNotificationId(notificationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notification not found: " + notificationId
                ));

        return mapToResponse(notification);
    }

    // Get notifications by recipient email
    @Transactional(readOnly = true)
    public List<Map<String, String>> getByRecipient(String recipient) {

        return notificationRepository.findByRecipient(recipient)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Convert entity into API response
    private Map<String, String> mapToResponse(Notification notification) {

        return Map.of(
                "notificationId", notification.getNotificationId(),
                "recipient", notification.getRecipient(),
                "status", notification.getStatus().name()
        );
    }
}