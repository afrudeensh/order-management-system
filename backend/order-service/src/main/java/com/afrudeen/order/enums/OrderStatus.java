package com.afrudeen.order.enums;

public enum OrderStatus {
    CREATED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED;

    public boolean canMoveTo(OrderStatus next) {
        return switch (this) {
            case CREATED   -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == SHIPPED || next == CANCELLED;
            case SHIPPED   -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
