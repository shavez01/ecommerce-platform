
package com.ecommerce.monolith.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.monolith.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);
}
