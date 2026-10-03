package com.afrudeen.order.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    public static final String TOPIC = "order-created";
    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);
    private final KafkaTemplate<String, OrderCreatedEvent> kafka;

    public OrderEventPublisher(KafkaTemplate<String, OrderCreatedEvent> kafka) {
        this.kafka = kafka;
    }

    public void publish(OrderCreatedEvent event) {

        kafka.send(TOPIC, String.valueOf(event.orderId()), event) // key = orderId

                .whenComplete((result, ex) -> {

                    if (ex != null) log.error("Kafka publish failed for order {}", event.orderId(), ex);

                    else log.info("Published order {} to partition {}", event.orderId(),
                            result.getRecordMetadata().partition());

                });
    }
}