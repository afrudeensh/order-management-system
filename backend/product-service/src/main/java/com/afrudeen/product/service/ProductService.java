package com.afrudeen.product.service;

import com.afrudeen.product.dto.ProductRequest;
import com.afrudeen.product.entity.Product;

import java.util.List;

public interface ProductService {

    Product create(ProductRequest productRequest);

    List<Product> findAll(String search);

    Product findById(Long id);

    Product  update(Long id, ProductRequest productRequest);

    void delete(Long id);

    void decreaseStock(Long id, int qty);

    void increaseStock(Long id, int qty);
}
