package com.ecommerce.invoice.entity;

import com.ecommerce.invoice.enums.InvoiceStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "invoices",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_invoices_invoice_id",
            columnNames = "invoice_id"
        ),
        @UniqueConstraint(
            name = "uk_invoices_order_id",
            columnNames = "order_id"
        )
    },
    indexes = {
        @Index(
            name = "idx_invoices_status",
            columnList = "status"
        )
    }
)
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false, length = 40)
    private String invoiceId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    protected Invoice() {
    }

    public static Invoice create(
            Long orderId,
            String productName,
            Integer quantity,
            BigDecimal totalAmount) {

        Invoice invoice = new Invoice();

        invoice.invoiceId = "INV-" + UUID.randomUUID();
        invoice.orderId = orderId;
        invoice.productName = productName;
        invoice.quantity = quantity;
        invoice.totalAmount = totalAmount;
        invoice.status = InvoiceStatus.GENERATED;
        invoice.generatedAt = LocalDateTime.now();

        return invoice;
    }

    public Long getId() {
        return id;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
}