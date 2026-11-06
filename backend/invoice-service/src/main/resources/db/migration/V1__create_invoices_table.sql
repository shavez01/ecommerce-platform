CREATE TABLE invoices (
    id BIGINT NOT NULL AUTO_INCREMENT,
    invoice_id VARCHAR(40) NOT NULL,
    order_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    generated_at DATETIME NOT NULL,

    CONSTRAINT pk_invoices PRIMARY KEY (id),
    CONSTRAINT uk_invoices_invoice_id UNIQUE (invoice_id),
    CONSTRAINT uk_invoices_order_id UNIQUE (order_id),

    INDEX idx_invoices_status (status)
);