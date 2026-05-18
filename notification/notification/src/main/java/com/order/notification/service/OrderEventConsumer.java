package com.order.notification.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.order.notification.model.OrderEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderEventConsumer {

    @KafkaListener(topics = "order-placed-topic", groupId = "notification-group")
    public void consumeOrderEvent(OrderEvent event) {

        log.info("Order Event Received");

        log.info("Order Id : {}", event.getOrderId());
        log.info("Order Number : {}", event.getOrderNumber());
        log.info("Customer Name : {}", event.getCustomerName());
        log.info("Product Name : {}", event.getProductName());
        log.info("Total Amount : {}", event.getTotalAmount());
        log.info("Status : {}", event.getStatus());
        log.info("Created At : {}", event.getCreatedAt());

        // Simulate notification sending
        log.info("Notification sent successfully for order : {}",
                event.getOrderNumber());
    }
}