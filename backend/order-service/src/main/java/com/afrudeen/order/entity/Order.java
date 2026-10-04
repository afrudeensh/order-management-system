package com.afrudeen.order.entity;

import com.afrudeen.order.common.BaseEntity;
import com.afrudeen.order.common.BusinessException;
import com.afrudeen.order.enums.OrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")

public class Order extends BaseEntity {

    @Column(nullable = false)
    private Long userId; // plain id, NOT a join to another service's table

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.CREATED;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() { }

    public Order(Long userId) {
        this.userId = userId;
    }

    public void addItem(OrderItem item) { // keeps both sides of the relation in sync
        items.add(item);
        item.setOrder(this);
    }

    public void changeStatus(OrderStatus next) {
        if (!status.canMoveTo(next)) {
            throw new BusinessException(
                    "Cannot change order from " + status + " to " + next);
        }
        this.status = next;
    }

    @Override
    public String getDisplayName() {
        return "Order #" + getId();
    }

    public Long getUserId() {
        return userId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
