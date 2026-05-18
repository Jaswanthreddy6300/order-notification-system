package com.order.notification.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private Long orderId;

    private String orderNumber;

    private String customerName;

    private String productName;

    private Double totalAmount;

    private String status;

    private LocalDateTime createdAt;
}