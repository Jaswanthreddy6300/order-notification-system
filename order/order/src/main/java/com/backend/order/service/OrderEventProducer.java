package com.backend.order.service;

import org.springframework.stereotype.Service;

import com.backend.order.model.OrderEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String TOPIC = "order-placed-topic";

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void sendOrderEvent(OrderEvent event) {
        kafkaTemplate.send(TOPIC, event.getOrderNumber(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send event for order: {} | reason: {}",
                                event.getOrderNumber(), ex.getMessage());
                    } else {
                        log.info("Event sent for order: {} | partition: {} | offset: {}",
                                event.getOrderNumber(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}