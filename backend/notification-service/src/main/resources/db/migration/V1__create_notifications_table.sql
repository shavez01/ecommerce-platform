CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    notification_id VARCHAR(36) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT uk_notifications_notification_id UNIQUE (notification_id),

    INDEX idx_notifications_recipient (recipient),
    INDEX idx_notifications_status (status)
);