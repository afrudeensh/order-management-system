package com.afrudeen.notification.handler;

import com.afrudeen.notification.dto.OrderCreatedEvent;
import com.afrudeen.notification.repository.NotificationRepository;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedAdminNotificationHandler extends NotificationHandler<OrderCreatedEvent> {

    public OrderCreatedAdminNotificationHandler(NotificationRepository repository) {
        super(repository);
    }

    @Override protected Long userIdOf(OrderCreatedEvent e) { return e.userId(); }
    @Override protected Long orderIdOf(OrderCreatedEvent e) { return e.orderId(); }
    @Override protected String audienceOf(OrderCreatedEvent e) { return "ADMIN"; }

    @Override
    protected String buildMessage(OrderCreatedEvent e) {
        return "New order #%d from customer #%d. Total: INR %s"
                .formatted(e.orderId(), e.userId(), e.totalAmount());
    }
}