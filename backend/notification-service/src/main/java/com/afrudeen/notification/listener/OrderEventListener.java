package com.afrudeen.notification.listener;
import com.afrudeen.notification.dto.LowStockAlert;
import com.afrudeen.notification.dto.OrderCreatedEvent;
import com.afrudeen.notification.handler.LowStockNotificationHandler;
import com.afrudeen.notification.handler.OrderCreatedAdminNotificationHandler;
import com.afrudeen.notification.handler.OrderCreatedNotificationHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private final OrderCreatedNotificationHandler customerHandler;
    private final OrderCreatedAdminNotificationHandler adminHandler;
    private final LowStockNotificationHandler lowStockHandler;

    public OrderEventListener(OrderCreatedNotificationHandler customerHandler,
                              OrderCreatedAdminNotificationHandler adminHandler,
                              LowStockNotificationHandler lowStockHandler) {
        this.customerHandler = customerHandler;
        this.adminHandler = adminHandler;
        this.lowStockHandler = lowStockHandler;
    }

    @KafkaListener(topics = "order-created", groupId = "notification-group")
    public void onOrderCreated(OrderCreatedEvent event) {
        customerHandler.handle(event);
        adminHandler.handle(event);

        if (event.lowStock() != null) {
            event.lowStock().forEach(text ->
                    lowStockHandler.handle(new LowStockAlert(event.orderId(), event.userId(), text)));
        }
    }
}
