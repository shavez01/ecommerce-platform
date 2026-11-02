
package com.ecommerce.monolith.product.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.monolith.product.entity.Product;
import com.ecommerce.monolith.product.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Create product
    @CacheEvict(cacheNames = {"productById", "products"}, allEntries = true)
    public Product createProduct(Product product) {

        if (productRepository.existsBySku(product.getSku())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Product SKU already exists");
        }

        return productRepository.save(product);
    }

    // Get product by ID - Redis cache
    @Cacheable(cacheNames = "productById", key = "#id")
    public Product getProductById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found with ID: " + id));
    }

    // Get all products - Redis cache
    @Cacheable(cacheNames = "products")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Update product
    @CacheEvict(cacheNames = {"productById", "products"}, allEntries = true)
    public Product updateProduct(Long id, Product updatedProduct) {

        Product existingProduct = getProductById(id);

        if (productRepository.existsBySkuAndIdNot(
                updatedProduct.getSku(), id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Product SKU already exists");
        }

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setSku(updatedProduct.getSku());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setActive(updatedProduct.isActive());

        return productRepository.save(existingProduct);
    }

    // Delete product
    @CacheEvict(cacheNames = {"productById", "products"}, allEntries = true)
    public void deleteProduct(Long id) {

        Product product = getProductById(id);

        productRepository.delete(product);
    }
}
