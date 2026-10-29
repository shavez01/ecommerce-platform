package com.ecommerce.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NotificationRequest(
        @Email @NotBlank String recipient,
        @NotBlank String subject,
        @NotBlank String message
) {}