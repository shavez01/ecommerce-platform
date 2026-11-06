
package com.ecommerce.payment.entity;

import com.ecommerce.payment.enums.PaymentStatus;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "payments",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_payments_payment_id",
            columnNames = "payment_id"
        )
    },
    indexes = {
        @Index(name = "idx_payments_order_id", columnList = "order_id"),
        @Index(name = "idx_payments_status", columnList = "status")
    }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false, length = 36)
    private String paymentId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "idempotency_key", length = 100, unique = true)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Payment() {
    }

    public static Payment create(
            String paymentId,
            Long orderId,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            String idempotencyKey,
            LocalDateTime now) {

        Payment payment = new Payment();

        payment.paymentId = paymentId;
        payment.orderId = orderId;
        payment.amount = amount;
        payment.currency = currency;
        payment.paymentMethod = paymentMethod;
        payment.idempotencyKey = idempotencyKey;
        payment.status = PaymentStatus.PENDING;
        payment.createdAt = now;
        payment.updatedAt = now;

        return payment;
    }

    public void transitionTo(PaymentStatus newStatus) {

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Payment status cannot be null"
            );
        }

        boolean allowed = switch (this.status) {
            case PENDING ->
                    newStatus == PaymentStatus.SUCCESS
                    || newStatus == PaymentStatus.FAILED;

            case SUCCESS ->
                    newStatus == PaymentStatus.REFUNDED;

            case FAILED, REFUNDED -> false;
        };

        if (!allowed) {
            throw new IllegalStateException(
                    "Invalid payment status transition: "
                    + this.status + " -> " + newStatus
            );
        }

        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void completePayment(
            PaymentStatus newStatus,
            String transactionReference,
            String failureReason) {

        if (newStatus != PaymentStatus.SUCCESS
                && newStatus != PaymentStatus.FAILED) {

            throw new IllegalArgumentException(
                    "Gateway can only complete payment as SUCCESS or FAILED"
            );
        }

        transitionTo(newStatus);

        this.transactionReference = transactionReference;
        this.failureReason = failureReason;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
