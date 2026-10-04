package com.afrudeen.order.controller;

import com.afrudeen.order.dto.request.CreateOrderRequest;
import com.afrudeen.order.dto.request.UpdateStatusRequest;
import com.afrudeen.order.dto.response.OrderResponse;
import com.afrudeen.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.afrudeen.order.common.ForbiddenException;

import java.util.List;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders")
public class OrderController {

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


    @GetMapping("/my")
    @Operation(summary = "My order history")
    public List<OrderResponse> mine(@RequestHeader("X-User-Id") Long userId) {
        return service.findMine(userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "One order (owner or admin)")
    public OrderResponse one(@PathVariable Long id,
                             @RequestHeader("X-User-Id") Long userId,
                             @RequestHeader("X-User-Role") String role) {
        return service.findById(id, userId, "ADMIN".equals(role));
    }

    @GetMapping
    @Operation(summary = "All orders (admin only)")
    public List<OrderResponse> all(@RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("Admin only");
        }
        return service.findAll();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Change order status (admin only)")
    public OrderResponse updateStatus(@PathVariable Long id,
                                      @RequestHeader("X-User-Role") String role,
                                      @Valid @RequestBody UpdateStatusRequest request) {
        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("Admin only");
        }
        return service.updateStatus(id, request.status());
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order (owner or admin)")
    public OrderResponse cancel(@PathVariable Long id,
                                @RequestHeader("X-User-Id") Long userId,
                                @RequestHeader("X-User-Role") String role) {
        return service.cancel(id, userId, "ADMIN".equals(role));
    }
}
