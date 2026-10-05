package com.afrudeen.product.controller;

import com.afrudeen.product.common.BaseResponse;
import com.afrudeen.product.dto.PageResponse;
import com.afrudeen.product.dto.ProductRequest;
import com.afrudeen.product.entity.Product;
import com.afrudeen.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "Product catalogue")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    private static final Set<String> SORTABLE = Set.of("id", "name", "price", "stock");

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
    public Product increaseStock(@PathVariable Long id, @RequestParam int quantity) {
        return service.increaseStock(id, quantity);
    }

    @GetMapping("/page")
    @Operation(summary = "Paged and sorted product list")
    public BaseResponse<PageResponse<Product>> page(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return BaseResponse.ok(service.page(search, pageable(page, size, sort, direction)));
    }

    private Pageable pageable(int page, int size, String sort, String direction) {
        String field = SORTABLE.contains(sort) ? sort : "name";
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        Sort order = Sort.by(dir, field);
        if (!field.equals("id")) {
            order = order.and(Sort.by("id"));      // tie-breaker, so pages never overlap
        }
        return (Pageable) PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), order);
    }
}