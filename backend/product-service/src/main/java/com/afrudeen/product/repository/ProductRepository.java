package com.afrudeen.product.repository;

import com.afrudeen.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // builds the SQL from the method name:
    // SELECT * FROM products WHERE LOWER(name) LIKE LOWER('%text%')
    List<Product> findByNameContainingIgnoreCase(String name);

    Optional<Product> findByIdAndIsActive(Long id, Boolean isActive);
}
