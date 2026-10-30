
package com.ecommerce.monolith.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.monolith.customer.entity.Customer;
import com.ecommerce.monolith.customer.service.CustomerService;
import com.ecommerce.monolith.product.entity.Product;
import com.ecommerce.monolith.product.service.ProductService;
import com.ecommerce.monolith.inventory.service.InventoryService;
import com.ecommerce.monolith.notification.service.NotificationService;
import com.ecommerce.monolith.order.dto.OrderRequest;
import com.ecommerce.monolith.order.entity.Order;
import com.ecommerce.monolith.order.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;

    public OrderService(
            OrderRepository orderRepository,
            CustomerService customerService,
            ProductService productService,
            InventoryService inventoryService,
            NotificationService notificationService) {

        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.notificationService = notificationService;
    }

    // Place order
    @Transactional
    public Order placeOrder(OrderRequest request) {

        // 1. Validate customer
        Customer customer = customerService.getCustomerById(request.getCustomerId());

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

        notificationService.recordNotification(
            savedOrder.getId(),
            "ORDER_PLACED",
            customer.getEmail(),
            "Order Confirmation",
            "Your order #" + savedOrder.getId()
                    + " has been placed successfully.");

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
