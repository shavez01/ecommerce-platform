package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductRequest;
// import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

        @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable("id") String id) {
        return productService.getProductById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(
            @Valid @RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    // @GetMapping
    // public List<ProductResponse> getAllProducts() {
    //     return productService.getAllProducts();
    // }

    // @GetMapping("/{id}")
    // public ProductResponse getProductById(@PathVariable("id") Long id) {
    //     return productService.getProductById(id);
    // }

    // @PostMapping
    // @ResponseStatus(HttpStatus.CREATED)
    // public ProductResponse createProduct(
    //         @Valid @RequestBody ProductRequest request) {
    //     return productService.createProduct(request);
    // }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(
            java.util.NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }
}
