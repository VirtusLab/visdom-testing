package com.example.store.service;

import com.example.store.controller.ProductController;
import com.example.store.exception.ProductNotFoundException;
import com.example.store.model.Product;
import com.example.store.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * AI-generated service that creates a cyclic dependency by depending on the controller.
 *
 * Violations:
 * 1. Service depends on Controller (creates a cycle: controller -> service -> controller)
 */
@Service
public class ProductService {

    private final ProductRepository repository;
    private final RestClient restClient;
    private final ProductController controller;

    public ProductService(ProductRepository repository, RestClient.Builder builder,
                          ProductController controller) {
        this.repository = repository;
        this.restClient = builder.baseUrl("http://inventory-service").build();
        this.controller = controller;
    }

    public Product findBySku(String sku) {
        return repository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }
}
