package com.afrudeen.notification.handler;

import com.afrudeen.notification.entity.Notification;
import com.afrudeen.notification.repository.NotificationRepository;
import org.slf4j.*;

public abstract class NotificationHandler<E> {

    private static final Logger log = LoggerFactory.getLogger(NotificationHandler.class);
    private final NotificationRepository repository;

    protected NotificationHandler(NotificationRepository repository) {
        this.repository = repository;
    }

    /** TEMPLATE METHOD: fixed steps, cannot be overridden. */
    public final void handle(E event) {
        validate(event);
        Notification n = new Notification(userIdOf(event), orderIdOf(event), buildMessage(event));
        afterSave(repository.save(n));
    }

    // steps that subclasses MUST provide
    protected abstract Long userIdOf(E event);
    protected abstract Long orderIdOf(E event);
    protected abstract String buildMessage(E event);
    // steps with a default that subclasses MAY override (hooks)

    protected void validate(E event) {
        if (event == null) throw new IllegalArgumentException("event is null");
    }
    protected void afterSave(Notification saved) {
        log.info("Saved: {}", saved.getDisplayName());
    }
}