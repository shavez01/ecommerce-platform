package com.ecommerce.inventory.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final ConcurrentHashMap<String, Integer> stock =
            new ConcurrentHashMap<>();

    public InventoryController() {
        stock.put("6ae4339490ed4ef5709aa761", 10);
    }

    @GetMapping("/{productId}")
    public Integer getStock(@PathVariable("productId") String productId) {
        Integer quantity = stock.get(productId);

        if (quantity == null) {
            throw new IllegalArgumentException("Product stock not found");
        }

        return quantity;
    }

    @PostMapping("/{productId}/reserve")
    @ResponseStatus(HttpStatus.OK)
    public Integer reserveStock(
            @PathVariable("productId") String productId,
            @RequestParam("quantity") int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero");
        }

        return stock.compute(productId, (id, available) -> {
            if (available == null) {
                throw new IllegalArgumentException(
                        "Product stock not found");
            }
            if (available < quantity) {
                throw new IllegalStateException("Insufficient stock");
            }
            return available - quantity;
        });
    }
}
