CREATE TABLE order_notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id VARCHAR(100) NOT NULL,
    product_name VARCHAR(255),
    quantity INT NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,

    CONSTRAINT pk_order_notifications PRIMARY KEY (id),
    CONSTRAINT uk_order_notifications_order_id UNIQUE (order_id),

    INDEX idx_order_notifications_status (status)
);