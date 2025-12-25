package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "product-service", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductResponse getProductById(@PathVariable("id") Long id);

    @PostMapping("/api/products/batch")
    List<ProductResponse> getProductsByIds(@RequestBody List<Long> ids);

    @PostMapping("/api/products/{id}/reserve")
    Boolean reserveStock(@PathVariable("id") Long id, @RequestParam("quantity") Integer quantity);

    @PostMapping("/api/products/{id}/release")
    void releaseStock(@PathVariable("id") Long id, @RequestParam("quantity") Integer quantity);

    @GetMapping("/api/products/{id}/stock")
    Boolean checkStock(@PathVariable("id") Long id, @RequestParam("quantity") Integer quantity);
}

