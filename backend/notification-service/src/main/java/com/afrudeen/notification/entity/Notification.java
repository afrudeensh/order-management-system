package com.afrudeen.notification.entity;

import com.afrudeen.notification.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false, length = 10,
            columnDefinition = "varchar(10) not null default 'USER'")
    private String audience = "USER";

    protected Notification() {
    }

    public Notification(Long userId, Long orderId, String message, String audience) {
        this.userId = userId;
        this.orderId = orderId;
        this.message = message;
        this.audience = audience;
    }

    public String getAudience() { return audience; }

    @Override
    public String getDisplayName() {
        return "Notification for order #" + orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getMessage() {
        return message;
    }
}
