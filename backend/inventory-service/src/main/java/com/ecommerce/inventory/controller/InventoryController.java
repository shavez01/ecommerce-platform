package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.service.InventoryService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public Integer getStock(@PathVariable String productId) {
        return inventoryService.getStock(productId);
    }

    @PostMapping("/{productId}/reserve")
    @ResponseStatus(HttpStatus.OK)
    public Integer reserveStock(
            @PathVariable String productId,
            @RequestParam int quantity) {

        return inventoryService.reserveStock(productId, quantity);
    }
}