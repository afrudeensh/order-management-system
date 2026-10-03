package com.afrudeen.notification.handler;

import com.afrudeen.notification.dto.OrderCreatedEvent;
import com.afrudeen.notification.repository.NotificationRepository;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedNotificationHandler extends NotificationHandler<OrderCreatedEvent> {

    public OrderCreatedNotificationHandler(NotificationRepository repository) {
        super(repository);
    }

    @Override
    protected Long userIdOf(OrderCreatedEvent e) {
        return e.userId();
    }

    @Override
    protected Long orderIdOf(OrderCreatedEvent e) {
        return e.orderId();
    }

    @Override
    protected String buildMessage(OrderCreatedEvent e) {
        return "Order #%d created successfully. Total: INR %s".formatted(e.orderId(), e.totalAmount());
    }
}
