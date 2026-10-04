package com.afrudeen.notification.dto;

public record LowStockAlert(
        Long orderId,
        Long userId,
        String text) { }