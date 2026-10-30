
package com.ecommerce.monolith.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.monolith.customer.service.CustomerService;
import com.ecommerce.monolith.product.entity.Product;
import com.ecommerce.monolith.product.service.ProductService;
import com.ecommerce.monolith.inventory.service.InventoryService;
import com.ecommerce.monolith.order.dto.OrderRequest;
import com.ecommerce.monolith.order.entity.Order;
import com.ecommerce.monolith.order.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final InventoryService inventoryService;

    public OrderService(
            OrderRepository orderRepository,
            CustomerService customerService,
            ProductService productService,
            InventoryService inventoryService) {

        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    // Place order
    @Transactional
    public Order placeOrder(OrderRequest request) {

        // 1. Validate request
        if (request.getCustomerId() == null
                || request.getProductId() == null
                || request.getQuantity() == null
                || request.getQuantity() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid order request");
        }

        // 2. Validate customer
        customerService.getCustomerById(request.getCustomerId());

        // 3. Validate product
        Product product =
                productService.getProductById(request.getProductId());

        if (!product.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product is not available");
        }

        // 4. Reduce inventory
        inventoryService.reduceStock(
                request.getProductId(),
                request.getQuantity());

        // 5. Calculate total amount
        BigDecimal totalAmount = product.getPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        // 6. Create order
        Order order = new Order();

        order.setCustomerId(request.getCustomerId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setTotalAmount(totalAmount);
        order.setStatus("PLACED");
        order.setCreatedAt(LocalDateTime.now());

        // 7. Save order
        return orderRepository.save(order);
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
}
