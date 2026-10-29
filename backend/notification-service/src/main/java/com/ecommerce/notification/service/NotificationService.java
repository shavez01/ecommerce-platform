package com.ecommerce.notification.service;

import com.ecommerce.notification.dto.NotificationRequest;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {

    public Map<String, String> send(NotificationRequest request) {
        return Map.of(
                "notificationId", UUID.randomUUID().toString(),
                "recipient", request.recipient(),
                "status", "ACCEPTED"
        );
    }
}