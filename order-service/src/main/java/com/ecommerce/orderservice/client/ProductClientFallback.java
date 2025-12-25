package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.dto.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class ProductClientFallback implements ProductClient {

    @Override
    public ProductResponse getProductById(Long id) {
        log.warn("Fallback: Unable to get product with id: {}", id);
        return ProductResponse.builder()
                .id(id)
                .name("Product Unavailable")
                .price(BigDecimal.ZERO)
                .quantity(0)
                .active(false)
                .build();
    }

    @Override
    public List<ProductResponse> getProductsByIds(List<Long> ids) {
        log.warn("Fallback: Unable to get products with ids: {}", ids);
        return ids.stream()
                .map(id -> ProductResponse.builder()
                        .id(id)
                        .name("Product Unavailable")
                        .price(BigDecimal.ZERO)
                        .quantity(0)
                        .active(false)
                        .build())
                .toList();
    }

    @Override
    public Boolean reserveStock(Long id, Integer quantity) {
        log.warn("Fallback: Unable to reserve stock for product: {}", id);
        return false;
    }

    @Override
    public void releaseStock(Long id, Integer quantity) {
        log.warn("Fallback: Unable to release stock for product: {}", id);
    }

    @Override
    public Boolean checkStock(Long id, Integer quantity) {
        log.warn("Fallback: Unable to check stock for product: {}", id);
        return false;
    }
}

