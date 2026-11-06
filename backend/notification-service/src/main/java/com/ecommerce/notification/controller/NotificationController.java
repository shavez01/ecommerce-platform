package com.ecommerce.notification.controller;

import com.ecommerce.notification.dto.NotificationRequest;
import com.ecommerce.notification.service.NotificationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Create notification
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> send(
            @Valid @RequestBody NotificationRequest request) {

        return notificationService.send(request);
    }

    // Get notification by ID
    @GetMapping("/{notificationId}")
    public Map<String, String> getByNotificationId(
            @PathVariable String notificationId) {

        return notificationService.getByNotificationId(notificationId);
    }

    // Get notifications by recipient
    @GetMapping("/recipient/{recipient}")
    public List<Map<String, String>> getByRecipient(
            @PathVariable String recipient) {

        return notificationService.getByRecipient(recipient);
    }
}