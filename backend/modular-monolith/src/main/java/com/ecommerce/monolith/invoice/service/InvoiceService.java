
package com.ecommerce.monolith.invoice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.monolith.customer.dto.CustomerResponse;
// import com.ecommerce.monolith.customer.entity.Customer;
import com.ecommerce.monolith.customer.service.CustomerService;
import com.ecommerce.monolith.order.entity.Order;
import com.ecommerce.monolith.order.service.OrderService;
import com.ecommerce.monolith.payment.entity.Payment;
import com.ecommerce.monolith.payment.service.PaymentService;
import com.ecommerce.monolith.notification.service.NotificationService;
import com.ecommerce.monolith.invoice.dto.InvoiceRequest;
import com.ecommerce.monolith.invoice.entity.Invoice;
import com.ecommerce.monolith.invoice.repository.InvoiceRepository;

@Service
public class InvoiceService {

    private static final BigDecimal TAX_RATE = new BigDecimal("18.00");

    private final InvoiceRepository invoiceRepository;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final CustomerService customerService;
    private final NotificationService notificationService;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            OrderService orderService,
            PaymentService paymentService,
            CustomerService customerService,
            NotificationService notificationService) {

        this.invoiceRepository = invoiceRepository;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.customerService = customerService;
        this.notificationService = notificationService;
    }

    // Generate invoice
    @Transactional
    public Invoice generateInvoice(InvoiceRequest request) {

        if (request.getOrderId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order ID is required");
        }

        Long orderId = request.getOrderId();

        // Prevent duplicate invoices
        if (invoiceRepository.existsByOrderId(orderId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invoice already exists for this order");
        }

        // Retrieve order
        Order order = orderService.getOrderById(orderId);

        // Verify successful payment
        Payment payment = paymentService.getPaymentByOrderId(orderId);

        if (!"SUCCESS".equals(payment.getStatus())
                || !"PAID".equals(order.getStatus())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invoice can only be generated for paid orders");
        }

        // Calculate subtotal
        BigDecimal subtotal = order.getTotalAmount();

        // Calculate GST
        BigDecimal taxAmount = subtotal
                .multiply(TAX_RATE)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        // Calculate grand total
        BigDecimal totalAmount = subtotal.add(taxAmount);

        // Generate invoice
        Invoice invoice = new Invoice();

        String invoiceNumber = "INV-"
                + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-"
                + UUID.randomUUID().toString()
                        .substring(0, 8).toUpperCase();

        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setOrderId(orderId);
        invoice.setSubtotal(subtotal);
        invoice.setTaxRate(TAX_RATE);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotalAmount(totalAmount);
        invoice.setCreatedAt(LocalDateTime.now());

        Invoice savedInvoice = invoiceRepository.save(invoice);

        CustomerResponse customer =
                customerService.getCustomerById(order.getCustomerId());

        notificationService.recordNotification(
                orderId,
                "INVOICE_GENERATED",
                customer.getEmail(),
                "Invoice Generated",
                "Invoice " + savedInvoice.getInvoiceNumber()
                        + " has been generated for your order.");

        return savedInvoice;
    }

    // Get invoice by ID
    public Invoice getInvoiceById(Long id) {

        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Invoice not found with ID: " + id));
    }

    // Get invoice by Order ID
    public Invoice getInvoiceByOrderId(Long orderId) {

        return invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Invoice not found for order ID: " + orderId));
    }

    // Get all invoices
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }
}
