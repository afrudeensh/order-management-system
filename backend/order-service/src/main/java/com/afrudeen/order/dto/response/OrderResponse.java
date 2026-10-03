package com.afrudeen.order.dto.response;

import com.afrudeen.order.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        BigDecimal totalAmount,
        String status,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {

    public static OrderResponse from(Order order) {

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getCreatedAt(),
                items
        );
    }
}