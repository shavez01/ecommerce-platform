package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new java.util.NoSuchElementException(
                                "Product not found: " + id));
    }

    public Product createProduct(ProductRequest request) {
        Product product = new Product(
                request.name(),
                request.price(),
                request.stock()
        );

        return productRepository.save(product);
    }
}

// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.atomic.AtomicLong;

// @Service
// public class ProductService {

//     private final ConcurrentHashMap<Long, ProductResponse> products =
//             new ConcurrentHashMap<>();

//     private final AtomicLong idGenerator = new AtomicLong();

//     public List<ProductResponse> getAllProducts() {
//         return List.copyOf(products.values());
//     }

//     public ProductResponse getProductById(Long id) {
//         ProductResponse product = products.get(id);

//         if (product == null) {
//             throw new java.util.NoSuchElementException(
//                     "Product not found with id: " + id);
//         }

//         return product;
//     }

//     public ProductResponse createProduct(ProductRequest request) {
//         Long id = idGenerator.incrementAndGet();

//         ProductResponse product = new ProductResponse(
//                 id,
//                 request.name(),
//                 request.price(),
//                 request.stock()
//         );

//         products.put(id, product);
//         return product;
//     }
// }
