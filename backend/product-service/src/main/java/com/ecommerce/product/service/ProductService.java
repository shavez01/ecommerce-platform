package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.mapper.ProductMapper;
import com.ecommerce.product.repository.ProductRepository;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(
            ProductRepository productRepository,
            ProductMapper productMapper) {

        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    public ProductResponse getProductById(@NonNull String id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Product not found: " + id));

        return productMapper.toResponse(product);
    }

    public ProductResponse createProduct(ProductRequest request) {

        Product product = new Product(
                request.name(),
                request.price(),
                request.stock()
        );

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }
}