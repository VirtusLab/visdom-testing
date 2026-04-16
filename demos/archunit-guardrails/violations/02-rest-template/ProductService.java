package com.example.store.service;

import com.example.store.exception.ProductNotFoundException;
import com.example.store.model.Product;
import com.example.store.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * AI-generated service using deprecated RestTemplate and field injection.
 *
 * Violations:
 * 1. Uses @Autowired field injection instead of constructor injection
 * 2. Depends on RestTemplate (deprecated since Spring 6.1)
 */
@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    public Product findBySku(String sku) {
        return repository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }

    public String checkInventory(String sku) {
        return restTemplate.getForObject("/inventory/" + sku, String.class);
    }
}
