package com.afrudeen.order.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderCreatedEvent(Long orderId,
                                Long userId,
                                BigDecimal totalAmount,
                                LocalDateTime createdAt,
                                List<String> lowStock) { }