package com.ecommerce.productservice.config;

import com.ecommerce.productservice.entity.Category;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.repository.CategoryRepository;
import com.ecommerce.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            log.info("Initializing sample data...");

            // Create categories
            Category electronics = categoryRepository.save(Category.builder()
                    .name("Electronics")
                    .description("Electronic devices and gadgets")
                    .build());

            Category smartphones = categoryRepository.save(Category.builder()
                    .name("Smartphones")
                    .description("Mobile phones and accessories")
                    .parent(electronics)
                    .build());

            Category laptops = categoryRepository.save(Category.builder()
                    .name("Laptops")
                    .description("Notebooks and laptops")
                    .parent(electronics)
                    .build());

            Category clothing = categoryRepository.save(Category.builder()
                    .name("Clothing")
                    .description("Men's and Women's clothing")
                    .build());

            Category menClothing = categoryRepository.save(Category.builder()
                    .name("Men's Clothing")
                    .description("Clothing for men")
                    .parent(clothing)
                    .build());

            // Create products
            productRepository.save(Product.builder()
                    .name("iPhone 15 Pro")
                    .description("Latest Apple smartphone with A17 Pro chip")
                    .price(new BigDecimal("999.99"))
                    .quantity(50)
                    .sku("IPHONE-15-PRO")
                    .category(smartphones)
                    .imageUrl("https://example.com/iphone15pro.jpg")
                    .build());

            productRepository.save(Product.builder()
                    .name("Samsung Galaxy S24")
                    .description("Samsung flagship with AI features")
                    .price(new BigDecimal("899.99"))
                    .quantity(75)
                    .sku("SAMSUNG-S24")
                    .category(smartphones)
                    .imageUrl("https://example.com/galaxys24.jpg")
                    .build());

            productRepository.save(Product.builder()
                    .name("MacBook Pro 14")
                    .description("Apple laptop with M3 Pro chip")
                    .price(new BigDecimal("1999.99"))
                    .quantity(30)
                    .sku("MACBOOK-PRO-14")
                    .category(laptops)
                    .imageUrl("https://example.com/macbookpro14.jpg")
                    .build());

            productRepository.save(Product.builder()
                    .name("Dell XPS 15")
                    .description("Premium Windows laptop")
                    .price(new BigDecimal("1599.99"))
                    .quantity(40)
                    .sku("DELL-XPS-15")
                    .category(laptops)
                    .imageUrl("https://example.com/dellxps15.jpg")
                    .build());

            productRepository.save(Product.builder()
                    .name("Classic T-Shirt")
                    .description("Cotton t-shirt for everyday wear")
                    .price(new BigDecimal("29.99"))
                    .quantity(200)
                    .sku("TSHIRT-001")
                    .category(menClothing)
                    .imageUrl("https://example.com/tshirt.jpg")
                    .build());

            log.info("Sample data initialized successfully!");
        }
    }
}

