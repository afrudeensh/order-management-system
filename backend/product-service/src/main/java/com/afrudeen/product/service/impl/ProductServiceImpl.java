package com.afrudeen.product.service.impl;

import com.afrudeen.product.common.BusinessException;
import com.afrudeen.product.common.ResourceNotFoundException;
import com.afrudeen.product.dto.PageResponse;
import com.afrudeen.product.dto.ProductRequest;
import com.afrudeen.product.entity.Product;
import com.afrudeen.product.repository.ProductRepository;
import com.afrudeen.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public Product create(ProductRequest request) {
        String name = request.name().trim();

        if (productRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("A product named \"" + name + "\" already exists");
        }

        Product saved = productRepository.save(new Product(
                name,
                request.price(),
                request.stock(),
                request.image(),
                request.color()));

        log.info("Created product {}", saved.getDisplayName());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll(String search) {
        if (search == null || search.isBlank()) {
            return productRepository.findByIsActiveTrue();
        }
        return productRepository.findByIsActiveTrueAndNameContainingIgnoreCase(search.trim());
    }

    /** Active products only, so a deleted product can no longer be sold. */
    @Override
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    @Override
    @Transactional
    public Product update(Long id, ProductRequest request) {
        String name = request.name().trim();
        Product product = findById(id);

        // Only check for duplicates when the name itself is being changed
        boolean renamed = !product.getName().equalsIgnoreCase(name);
        if (renamed && productRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("A product named \"" + name + "\" already exists");
        }

        product.setName(name);
        product.setPrice(request.price());
        product.setImage(request.image());
        product.setColor(request.color());
        // stock is NOT changed here: use increaseStock or the order flow
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        product.setIsActive(false);
        // free the name, so the unique constraint doesn't block re-creating it later
        product.setName(product.getName() + " [deleted #" + id + "]");
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void decreaseStock(Long id, int qty) {
        if (qty <= 0) {
            throw new BusinessException("Quantity must be at least 1");
        }
        findById(id);   // 404 if the product doesn't exist or was deleted
        if (productRepository.decreaseStock(id, qty) == 0) {
            throw new BusinessException("Insufficient stock for product " + id);
        }
    }

    @Override
    @Transactional
    public Product increaseStock(Long id, int qty) {
        if (qty <= 0) {
            throw new BusinessException("Quantity must be at least 1");
        }
        // Deleted products are allowed here, so cancelling an old order
        // that contains one can still return its stock.
        getAny(id);
        productRepository.increaseStock(id, qty);   // atomic: stock = stock + qty
        return getAny(id);                          // fresh row
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<Product> page(String search, Pageable pageable) {
        Page<Product> result = (search == null || search.isBlank())
                ? productRepository.findByIsActiveTrue(pageable)
                : productRepository.findByIsActiveTrueAndNameContainingIgnoreCase(search.trim(), pageable);
        return PageResponse.from(result);
    }

    private Product getAny(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }
}