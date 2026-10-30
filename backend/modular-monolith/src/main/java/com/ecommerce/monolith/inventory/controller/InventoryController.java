
package com.ecommerce.monolith.inventory.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.monolith.inventory.entity.Inventory;
import com.ecommerce.monolith.inventory.service.InventoryService;

@RestController
@RequestMapping("/api/inventory")
@Validated
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // Create inventory
    @PostMapping
    public ResponseEntity<Inventory> createInventory(
            @Valid @RequestBody Inventory inventory) {

        Inventory createdInventory =
                inventoryService.createInventory(inventory);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdInventory);
    }

    // Get all inventory
    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {

        return ResponseEntity.ok(
                inventoryService.getAllInventory());
    }

    // Get inventory by product ID
    @GetMapping("/{productId}")
    public ResponseEntity<Inventory> getInventoryByProductId(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                inventoryService.getInventoryByProductId(productId));
    }

    // Add stock
    @PatchMapping("/{productId}/add")
    public ResponseEntity<Inventory> addStock(
            @PathVariable @Positive(message = "Product ID must be positive") 
            Long productId,

            @RequestParam @Positive(message = "Quantity must be greater than zero") 
            Integer quantity) {

        return ResponseEntity.ok(
                inventoryService.addStock(productId, quantity));
    }

    // Reduce stock
    @PatchMapping("/{productId}/reduce")
    public ResponseEntity<Inventory> reduceStock(
            @PathVariable @Positive(message = "Product ID must be positive")
            Long productId,

            @RequestParam @Positive(message = "Quantity must be greater than zero") Integer quantity) {

        return ResponseEntity.ok(
                inventoryService.reduceStock(productId, quantity));
    }

    // Health check
    @GetMapping("/health")
    public String health() {
        return "Inventory module is running";
    }
}
