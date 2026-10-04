package com.afrudeen.product.service.impl;

import com.afrudeen.product.common.BusinessException;
import com.afrudeen.product.dto.ProductRequest;
import com.afrudeen.product.entity.Product;
import com.afrudeen.product.repository.ProductRepository;
import com.afrudeen.product.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product create(ProductRequest productRequest) {

        String name = productRequest.name().trim();
        if (productRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("A product named \"" + name + "\" already exists");
        }

        Product saved = productRepository
                .save(new Product(productRequest.name(),
                        productRequest.price(),
                        productRequest.stock()));

        log.info("Created product {}", saved.getDisplayName());
        return saved;

    }

    @Override
    public List<Product> findAll(String search) {

        if (search == null || search.isBlank())
            return productRepository.findAll();

        return productRepository.findByNameContainingIgnoreCase(search);
    }

    @Override
    public Product findById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id " + id));
    }

    @Override
    @Transactional
    public Product update(Long id, ProductRequest productRequest) {

        String name = productRequest.name().trim();
        if (productRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BusinessException("A product named \"" + name + "\" already exists");
        }
        Product product = findById(id);
        product.setName(productRequest.name());
        product.setPrice(productRequest.price());
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id " + id));
        product.setIsActive(false);
        productRepository.save(product);
    }

    @Transactional
    public void decreaseStock(Long id, int qty) {
        findById(id);   // throws 404 if the product doesn't exist
        if (productRepository.decreaseStock(id, qty) == 0) {
            throw new BusinessException("Insufficient stock for product " + id);
        }
    }

    @Transactional
    public Product increaseStock(Long id, int qty) {
        if (qty <= 0) {
            throw new BusinessException("Quantity must be at least 1");
        }
        findById(id);                            // 404 if missing
        productRepository.increaseStock(id, qty);       // atomic: stock = stock + qty
        return findById(id);                     // fresh row with the new stock
    }
}
