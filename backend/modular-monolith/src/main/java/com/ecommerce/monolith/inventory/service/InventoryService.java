
package com.ecommerce.monolith.inventory.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.monolith.inventory.entity.Inventory;
import com.ecommerce.monolith.inventory.repository.InventoryRepository;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    // Create inventory for a product
    public Inventory createInventory(Inventory inventory) {

        if (inventory.getAvailableQuantity() == null
                || inventory.getAvailableQuantity() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity cannot be negative or null");
        }

        if (inventoryRepository.existsByProductId(inventory.getProductId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Inventory already exists for product ID: "
                            + inventory.getProductId());
        }

        return inventoryRepository.save(inventory);
    }

    // Get inventory by product ID
    public Inventory getInventoryByProductId(Long productId) {

        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inventory not found for product ID: " + productId));
    }

    // Get all inventory records
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    // Add stock
    @Transactional
    public Inventory addStock(Long productId, Integer quantity) {

        validateQuantity(quantity);

        Inventory inventory = getInventoryByProductId(productId);

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() + quantity);

        return inventoryRepository.save(inventory);
    }

    // Reduce stock
    @Transactional
    public Inventory reduceStock(Long productId, Integer quantity) {

        int updatedRows = inventoryRepository
                .reduceStockIfAvailable(productId, quantity);

        if (updatedRows == 0) {

            if (!inventoryRepository.existsByProductId(productId)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inventory not found");
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Insufficient stock available");
        }

        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inventory not found"));
    }

    private void validateQuantity(Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero");
        }
    }
}
