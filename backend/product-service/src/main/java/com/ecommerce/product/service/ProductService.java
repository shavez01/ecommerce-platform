package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {

    private final ConcurrentHashMap<Long, ProductResponse> products =
            new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong();

    public List<ProductResponse> getAllProducts() {
        return List.copyOf(products.values());
    }

    public ProductResponse getProductById(Long id) {
        ProductResponse product = products.get(id);

        if (product == null) {
            throw new java.util.NoSuchElementException(
                    "Product not found with id: " + id);
        }

        return product;
    }

    public ProductResponse createProduct(ProductRequest request) {
        Long id = idGenerator.incrementAndGet();

        ProductResponse product = new ProductResponse(
                id,
                request.name(),
                request.price(),
                request.stock()
        );

        products.put(id, product);
        return product;
    }
}
