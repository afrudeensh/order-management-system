package com.afrudeen.order.dto.response;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer stock
) {
}