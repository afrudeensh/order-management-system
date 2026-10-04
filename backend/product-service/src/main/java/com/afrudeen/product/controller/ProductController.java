package com.afrudeen.product.controller;

import com.afrudeen.product.common.BaseResponse;
import com.afrudeen.product.dto.ProductRequest;
import com.afrudeen.product.entity.Product;
import com.afrudeen.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "Product catalogue")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a product")
    public BaseResponse<Product> create(@Valid @RequestBody ProductRequest request) {
        return BaseResponse.created(service.create(request));
    }

    @GetMapping
    @Operation(summary = "List products, optionally filtered by name")
    public BaseResponse<List<Product>> findAll(@RequestParam(required = false) String search) {
        return BaseResponse.ok(service.findAll(search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one product")
    public BaseResponse<Product> findById(@PathVariable Long id) {
        return BaseResponse.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    public BaseResponse<Product> update(@PathVariable Long id,
                                        @Valid @RequestBody ProductRequest request) {
        return BaseResponse.ok("Product updated", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product")
    public BaseResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return BaseResponse.ok("Product deleted", null);
    }

    @PutMapping("/{id}/stock/decrease")
    public void decreaseStock(@PathVariable Long id, @RequestParam int quantity) {
        service.decreaseStock(id, quantity);
    }

    @PutMapping("/{id}/stock/increase")
    public void increaseStock(@PathVariable Long id, @RequestParam int quantity) {
        service.increaseStock(id, quantity);
    }
}