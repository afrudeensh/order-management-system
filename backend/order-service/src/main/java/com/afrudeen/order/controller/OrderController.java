package com.afrudeen.order.controller;

import com.afrudeen.order.common.ForbiddenException;
import com.afrudeen.order.dto.request.CreateOrderRequest;
import com.afrudeen.order.dto.request.UpdateStatusRequest;
import com.afrudeen.order.dto.response.OrderResponse;
import com.afrudeen.order.dto.response.PageResponse;
import com.afrudeen.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders")
public class OrderController {

    private static final Set<String> SORTABLE =
            Set.of("id", "createdAt", "totalAmount", "status");

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Place an order (user id comes from the JWT via the Gateway)")
    public OrderResponse create(@RequestHeader("X-User-Id") Long userId,
                                @Valid @RequestBody CreateOrderRequest request) {
        return service.create(userId, request);
    }

    @GetMapping
    @Operation(summary = "All orders (admin only)")
    public List<OrderResponse> all(@RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return service.findAll();
    }

    @GetMapping("/my")
    @Operation(summary = "My order history")
    public List<OrderResponse> mine(@RequestHeader("X-User-Id") Long userId) {
        return service.findMine(userId);
    }

    @GetMapping("/my/page")
    @Operation(summary = "My orders, paged and sorted")
    public PageResponse<OrderResponse> minePage(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        return service.findMinePage(userId, pageable(page, size, sort, direction));
    }

    @GetMapping("/page")
    @Operation(summary = "All orders, paged and sorted (admin only)")
    public PageResponse<OrderResponse> allPage(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        requireAdmin(role);
        return service.findAllPage(pageable(page, size, sort, direction));
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "One order (owner or admin)")
    public OrderResponse one(@PathVariable Long id,
                             @RequestHeader("X-User-Id") Long userId,
                             @RequestHeader("X-User-Role") String role) {
        return service.findById(id, userId, "ADMIN".equals(role));
    }

    @PutMapping("/{id:\\d+}/status")
    @Operation(summary = "Change order status (admin only)")
    public OrderResponse updateStatus(@PathVariable Long id,
                                      @RequestHeader("X-User-Role") String role,
                                      @Valid @RequestBody UpdateStatusRequest request) {
        requireAdmin(role);
        return service.updateStatus(id, request.status());
    }

    @PutMapping("/{id:\\d+}/cancel")
    @Operation(summary = "Cancel an order (owner or admin)")
    public OrderResponse cancel(@PathVariable Long id,
                                @RequestHeader("X-User-Id") Long userId,
                                @RequestHeader("X-User-Role") String role) {
        return service.cancel(id, userId, "ADMIN".equals(role));
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("Admin only");
        }
    }

    private Pageable pageable(int page, int size, String sort, String direction) {
        String field = SORTABLE.contains(sort) ? sort : "createdAt";
        Sort.Direction dir = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort order = Sort.by(dir, field);
        if (!field.equals("id")) {
            order = order.and(Sort.by(Sort.Direction.DESC, "id"));   // tie-breaker
        }
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), order);
    }
}