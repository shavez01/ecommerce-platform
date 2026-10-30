
package com.ecommerce.monolith.product.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;

@Entity
@Table(name = "monolith_products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100,
        message = "Product name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @Size(max = 1000,
        message = "Description cannot exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU cannot exceed 100 characters")
    @Column(nullable = false, unique = true)
    private String sku;

    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.01",
            message = "Product price must be greater than zero")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active = true;

    // Default constructor required by JPA
    public Product() {
    }

    public Product(String name, String description, String sku,
                   BigDecimal price, boolean active) {
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.price = price;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
