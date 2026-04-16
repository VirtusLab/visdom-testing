package com.example.store.controller;

import com.example.store.model.Product;
import com.example.store.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/{sku}")
    public Product getBySku(@PathVariable String sku) {
        return service.findBySku(sku);
    }
}
