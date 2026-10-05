package com.afrudeen.order.entity;

import com.afrudeen.order.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "pending_stock_restores")
public class PendingStockRestore extends BaseEntity {

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, length = 100)
    private String reason;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(nullable = false)
    private boolean done = false;

    protected PendingStockRestore() { }

    public PendingStockRestore(Long productId, int quantity, String reason) {
        this.productId = productId;
        this.quantity = quantity;
        this.reason = reason;
    }

    @Override
    public String getDisplayName() {
        return "Stock restore for product " + productId;
    }

    public Long getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }
    public String getReason() { return reason; }
    public int getAttempts() { return attempts; }
    public boolean isDone() { return done; }

    public void markDone() { this.done = true; }
    public void recordFailure() { this.attempts++; }
}