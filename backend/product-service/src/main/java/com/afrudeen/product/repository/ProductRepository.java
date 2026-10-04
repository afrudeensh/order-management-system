package com.afrudeen.product.repository;

import com.afrudeen.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // builds the SQL from the method name:
    // SELECT * FROM products WHERE LOWER(name) LIKE LOWER('%text%')
    List<Product> findByNameContainingIgnoreCase(String name);

    Optional<Product> findByIdAndIsActive(Long id, Boolean isActive);

    @Modifying(clearAutomatically = true)
    @Query("update Product p set p.stock = p.stock - :qty where p.id = :id and p.stock >= :qty")
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);

    @Modifying(clearAutomatically = true)
    @Query("update Product p set p.stock = p.stock + :qty where p.id = :id")
    int increaseStock(@Param("id") Long id, @Param("qty") int qty);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
