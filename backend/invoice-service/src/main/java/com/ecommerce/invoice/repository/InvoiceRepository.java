package com.ecommerce.invoice.repository;

import com.ecommerce.invoice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceId(String invoiceId);

    Optional<Invoice> findByOrderId(Long orderId);

    List<Invoice> findAllByStatus(
            com.ecommerce.invoice.enums.InvoiceStatus status
    );
}