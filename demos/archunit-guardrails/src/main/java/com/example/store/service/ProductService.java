package com.example.store.service;

import com.example.store.exception.ProductNotFoundException;
import com.example.store.model.Product;
import com.example.store.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final RestClient restClient;

    public ProductService(ProductRepository repository, RestClient.Builder builder) {
        this.repository = repository;
        this.restClient = builder.baseUrl("http://inventory-service").build();
    }

    public Product findBySku(String sku) {
        return repository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }
}
