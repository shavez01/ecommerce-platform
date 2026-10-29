package com.ecommerce.monolith.order;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "module", "Order",
            "status", "UP"
        );
    }
}
