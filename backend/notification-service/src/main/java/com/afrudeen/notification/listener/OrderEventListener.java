package com.afrudeen.notification.listener;
import com.afrudeen.notification.dto.OrderCreatedEvent;
import com.afrudeen.notification.handler.OrderCreatedNotificationHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private final OrderCreatedNotificationHandler handler;

    public OrderEventListener(OrderCreatedNotificationHandler handler) {
        this.handler = handler;
    }

    @KafkaListener(topics = "order-created", groupId = "notification-group")

    public void onOrderCreated(OrderCreatedEvent event) {
        handler.handle(event);
    }
}
