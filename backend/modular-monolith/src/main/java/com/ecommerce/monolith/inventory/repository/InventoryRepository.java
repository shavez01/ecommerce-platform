
package com.ecommerce.monolith.inventory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.monolith.inventory.entity.Inventory;

public interface InventoryRepository
        extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    boolean existsByProductId(Long productId);

    @Modifying
    @Query("""
        UPDATE Inventory i
        SET i.availableQuantity = i.availableQuantity - :quantity
        WHERE i.productId = :productId
          AND i.availableQuantity >= :quantity
    """)
    int reduceStockIfAvailable(
            @Param("productId") Long productId,
            @Param("quantity") Integer quantity);
}
