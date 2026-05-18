package com.backend.order.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.backend.order.DTOs.OrderRequest;
import com.backend.order.DTOs.OrderResponse;
import com.backend.order.model.Order;
import com.backend.order.model.OrderEvent;
import com.backend.order.model.OrderStatus;
import com.backend.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository oRepo;

    private final OrderEventProducer eventProducer;

    public OrderResponse addOrder(OrderRequest o) {

        Order order = new Order();

        order.setOrderNumber(UUID.randomUUID().toString());
        order.setCustomerName(o.getCustomerName());
        order.setProductName(o.getProductName());
        order.setQuantity(o.getQuantity());
        order.setTotalAmount(o.getTotalAmount());
        order.setStatus(OrderStatus.PLACED);

        Order savedOrder = oRepo.save(order);

        // Kafka Event
        OrderEvent event = new OrderEvent();

        event.setOrderId(savedOrder.getId());
        event.setOrderNumber(savedOrder.getOrderNumber());
        event.setCustomerName(savedOrder.getCustomerName());
        event.setProductName(savedOrder.getProductName());
        event.setTotalAmount(savedOrder.getTotalAmount());
        event.setStatus(savedOrder.getStatus().name());
        event.setCreatedAt(savedOrder.getCreatedAt());

        eventProducer.sendOrderEvent(event);

        // Response
        OrderResponse response = new OrderResponse();

        response.setId(savedOrder.getId());
        response.setOrderNumber(savedOrder.getOrderNumber());
        response.setCustomerName(savedOrder.getCustomerName());
        response.setProductName(savedOrder.getProductName());
        response.setQuantity(savedOrder.getQuantity());
        response.setTotalAmount(savedOrder.getTotalAmount());
        response.setStatus(savedOrder.getStatus());
        response.setCreatedAt(savedOrder.getCreatedAt());
        response.setUpdatedAt(savedOrder.getUpdatedAt());

        return response;
    }

    public List<OrderResponse> getAllOrders() {

        List<Order> orders = oRepo.findAll();

        List<OrderResponse> responseList = new ArrayList<>();

        for (Order order : orders) {

            OrderResponse response = new OrderResponse();

            response.setId(order.getId());
            response.setOrderNumber(order.getOrderNumber());
            response.setCustomerName(order.getCustomerName());
            response.setProductName(order.getProductName());
            response.setQuantity(order.getQuantity());
            response.setTotalAmount(order.getTotalAmount());
            response.setStatus(order.getStatus());
            response.setCreatedAt(order.getCreatedAt());
            response.setUpdatedAt(order.getUpdatedAt());

            responseList.add(response);
        }

        return responseList;
    }

    public OrderResponse getOrderById(Long id) {

        Optional<Order> optionalOrder = oRepo.findById(id);

        if (optionalOrder.isPresent()) {

            Order order = optionalOrder.get();

            OrderResponse response = new OrderResponse();

            response.setId(order.getId());
            response.setOrderNumber(order.getOrderNumber());
            response.setCustomerName(order.getCustomerName());
            response.setProductName(order.getProductName());
            response.setQuantity(order.getQuantity());
            response.setTotalAmount(order.getTotalAmount());
            response.setStatus(order.getStatus());
            response.setCreatedAt(order.getCreatedAt());
            response.setUpdatedAt(order.getUpdatedAt());

            return response;
        }

        throw new RuntimeException("Order not found with id : " + id);
    }

    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {

        Optional<Order> optionalOrder = oRepo.findById(id);

        if (optionalOrder.isPresent()) {

            Order order = optionalOrder.get();

            order.setStatus(status);

            Order updatedOrder = oRepo.save(order);

            OrderResponse response = new OrderResponse();

            response.setId(updatedOrder.getId());
            response.setOrderNumber(updatedOrder.getOrderNumber());
            response.setCustomerName(updatedOrder.getCustomerName());
            response.setProductName(updatedOrder.getProductName());
            response.setQuantity(updatedOrder.getQuantity());
            response.setTotalAmount(updatedOrder.getTotalAmount());
            response.setStatus(updatedOrder.getStatus());
            response.setCreatedAt(updatedOrder.getCreatedAt());
            response.setUpdatedAt(updatedOrder.getUpdatedAt());

            return response;
        }

        throw new RuntimeException("Order not found with id : " + id);
    }
}