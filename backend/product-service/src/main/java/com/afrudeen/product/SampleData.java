package com.afrudeen.product;

import com.afrudeen.product.entity.Product;
import com.afrudeen.product.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;

@Configuration
public class SampleData {
    @Bean
    CommandLineRunner seedProducts(ProductRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(new Product("Fuel Injector", new BigDecimal("1299.00"), 20));
                repo.save(new Product("Oil Filter", new BigDecimal("349.50"), 100));
                repo.save(new Product("Piston Ring Set", new BigDecimal("2150.00"), 15));
            }
        };
    }
}