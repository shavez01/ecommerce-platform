package com.ecommerce.monolith.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.monolith.inventory.entity.Inventory;
import com.ecommerce.monolith.inventory.repository.InventoryRepository;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock InventoryRepository inventoryRepository;
    private InventoryService service;

    @BeforeEach
    void setUp() {
        service = new InventoryService(inventoryRepository);
    }

    @Test
    void reduceStockReturnsUpdatedInventory() {
        Inventory updated = new Inventory();
        updated.setProductId(20L);
        updated.setAvailableQuantity(7);
        when(inventoryRepository.reduceStockIfAvailable(20L, 3)).thenReturn(1);
        when(inventoryRepository.findByProductId(20L)).thenReturn(Optional.of(updated));

        Inventory result = service.reduceStock(20L, 3);

        assertEquals(7, result.getAvailableQuantity());
    }

    @Test
    void reduceStockReturnsNotFoundWhenInventoryMissing() {
        when(inventoryRepository.reduceStockIfAvailable(20L, 3)).thenReturn(0);
        when(inventoryRepository.existsByProductId(20L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.reduceStock(20L, 3));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void reduceStockReturnsConflictWhenQuantityInsufficient() {
        when(inventoryRepository.reduceStockIfAvailable(20L, 9)).thenReturn(0);
        when(inventoryRepository.existsByProductId(20L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.reduceStock(20L, 9));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void addStockIncreasesQuantity() {
        Inventory inventory = new Inventory();
        inventory.setProductId(20L);
        inventory.setAvailableQuantity(5);
        when(inventoryRepository.findByProductId(20L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(inventory)).thenReturn(inventory);

        Inventory result = service.addStock(20L, 4);

        assertEquals(9, result.getAvailableQuantity());
    }
}
