package com.ecommerce.notification.entity;

import com.ecommerce.notification.enums.NotificationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "notifications",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_notifications_notification_id",
            columnNames = "notification_id"
        )
    },
    indexes = {
        @Index(name = "idx_notifications_recipient", columnList = "recipient"),
        @Index(name = "idx_notifications_status", columnList = "status")
    }
)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id", nullable = false, length = 36)
    private String notificationId;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Notification() {
    }

    public static Notification create(
            String notificationId,
            String recipient,
            String subject,
            String message,
            LocalDateTime now) {

        Notification notification = new Notification();

        notification.notificationId = notificationId;
        notification.recipient = recipient;
        notification.subject = subject;
        notification.message = message;
        notification.status = NotificationStatus.ACCEPTED;
        notification.createdAt = now;
        notification.updatedAt = now;

        return notification;
    }

    public Long getId() {
        return id;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
