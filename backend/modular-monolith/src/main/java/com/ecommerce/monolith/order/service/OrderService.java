
package com.ecommerce.monolith.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.monolith.customer.dto.CustomerResponse;
// import com.ecommerce.monolith.customer.entity.Customer;
import com.ecommerce.monolith.customer.service.CustomerService;
import com.ecommerce.monolith.kafka.event.OrderPlacedEvent;
import com.ecommerce.monolith.kafka.producer.OrderEventProducer;
import com.ecommerce.monolith.product.entity.Product;
import com.ecommerce.monolith.product.service.ProductService;
import com.ecommerce.monolith.inventory.service.InventoryService;
import com.ecommerce.monolith.notification.service.NotificationService;
import com.ecommerce.monolith.order.dto.OrderRequest;
import com.ecommerce.monolith.order.entity.Order;
import com.ecommerce.monolith.order.repository.OrderRepository;

@Service
public class OrderService {

    private static final Logger log =
        LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final OrderEventProducer orderEventProducer;
    private final NotificationService notificationService;

    public OrderService(
            OrderRepository orderRepository,
            CustomerService customerService,
            ProductService productService,
            InventoryService inventoryService,
            OrderEventProducer orderEventProducer,
            NotificationService notificationService) {

        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.orderEventProducer = orderEventProducer;
        this.notificationService = notificationService;
    }

    // Place order
    @Transactional
    public Order placeOrder(OrderRequest request) {

        // 1. Validate customer
        CustomerResponse customer = customerService.getCustomerById(request.getCustomerId());

        // 2. Validate product
        Product product =
                productService.getProductById(request.getProductId());

        if (!product.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product is not available");
        }

        // 3. Reduce inventory
        inventoryService.reduceStock(
                request.getProductId(),
                request.getQuantity());

        // 4. Calculate total amount
        BigDecimal totalAmount = product.getPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        // 5. Create order
        Order order = new Order();

        order.setCustomerId(request.getCustomerId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setTotalAmount(totalAmount);
        order.setStatus("PLACED");
        order.setCreatedAt(LocalDateTime.now());

        // 6. Save order
        Order savedOrder = orderRepository.save(order);

        // notificationService.recordNotification(
        //     savedOrder.getId(),
        //     "ORDER_PLACED",
        //     customer.getEmail(),
        //     "Order Confirmation",
        //     "Your order #" + savedOrder.getId()
        //             + " has been placed successfully.");

        // Prepare the event
        OrderPlacedEvent event = new OrderPlacedEvent(
                savedOrder.getId(),
                savedOrder.getCustomerId(),
                savedOrder.getTotalAmount(),
                savedOrder.getCreatedAt()
        );

        // Publish event only after database transaction commits
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {

                        orderEventProducer.publishOrderPlacedEvent(event)
                                .whenComplete((result, exception) -> {

                                    if (exception != null) {
                                        log.error(
                                                "Failed to publish OrderPlacedEvent for orderId: {}",
                                                event.orderId(),
                                                exception
                                        );
                                    } else {
                                        log.info(
                                                "OrderPlacedEvent published successfully. OrderId: {}",
                                                event.orderId()
                                        );
                                    }
                                });
                    }
                }
        );


        return savedOrder;
    }

    // Get order by ID
    public Order getOrderById(Long id) {

        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found with ID: " + id));
    }

    // Get all orders
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Order markOrderAsPaid(Long orderId) {

        Order order = getOrderById(orderId);

        if (!"PLACED".equals(order.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only placed orders can be marked as paid");
        }

        order.setStatus("PAID");

        return orderRepository.save(order);
    }
}
