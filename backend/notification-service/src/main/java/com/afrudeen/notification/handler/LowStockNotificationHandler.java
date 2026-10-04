package com.afrudeen.notification.handler;

import com.afrudeen.notification.dto.LowStockAlert;
import com.afrudeen.notification.repository.NotificationRepository;
import org.springframework.stereotype.Component;

@Component
public class LowStockNotificationHandler extends NotificationHandler<LowStockAlert> {

    public LowStockNotificationHandler(NotificationRepository repository) {
        super(repository);
    }

    @Override protected Long userIdOf(LowStockAlert e) { return e.userId(); }
    @Override protected Long orderIdOf(LowStockAlert e) { return e.orderId(); }
    @Override protected String audienceOf(LowStockAlert e) { return "ADMIN"; }
    @Override protected String buildMessage(LowStockAlert e) { return "Low stock: " + e.text(); }
}