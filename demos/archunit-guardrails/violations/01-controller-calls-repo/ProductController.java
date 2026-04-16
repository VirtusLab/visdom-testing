package com.example.store.controller;

import com.example.store.model.Product;
import com.example.store.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI-generated controller that bypasses the service layer.
 *
 * Violations:
 * 1. Controller directly depends on Repository (bypasses service layer)
 * 2. Uses @Autowired field injection instead of constructor injection
 * 3. Throws generic RuntimeException instead of specific ProductNotFoundException
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository repository;

    @GetMapping("/{sku}")
    public Product getBySku(@PathVariable String sku) {
        return repository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Not found"));
    }
}
