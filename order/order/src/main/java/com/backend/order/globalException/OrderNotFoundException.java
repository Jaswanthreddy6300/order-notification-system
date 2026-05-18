package com.backend.order.globalException;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String m) {
        super(m);
    }
}
