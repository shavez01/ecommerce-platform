
CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT,

    payment_id VARCHAR(36) NOT NULL,
    order_id BIGINT NOT NULL,

    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',

    payment_method VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,

    transaction_reference VARCHAR(100),
    failure_reason VARCHAR(255),

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    CONSTRAINT pk_payments PRIMARY KEY (id),
    CONSTRAINT uk_payments_payment_id UNIQUE (payment_id),

    INDEX idx_payments_order_id (order_id),
    INDEX idx_payments_status (status)
);
