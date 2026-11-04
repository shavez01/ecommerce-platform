package com.ecommerce.product.mapper;

import org.springframework.stereotype.Component;

import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Product;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock()
        );
    }
}