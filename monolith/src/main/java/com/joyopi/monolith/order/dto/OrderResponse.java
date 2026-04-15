package com.joyopi.monolith.order.dto;

import com.joyopi.monolith.order.domain.Order;
import com.joyopi.monolith.order.domain.OrderStatus;

import java.time.LocalDateTime;

public record OrderResponse(Long id, Long userId, Long productId, int totalAmount, int pointAmount,
                             OrderStatus status, LocalDateTime createdAt) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getProductId(),
                order.getTotalAmount(),
                order.getPointAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
