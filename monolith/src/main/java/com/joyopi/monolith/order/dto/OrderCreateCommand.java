package com.joyopi.monolith.order.dto;

import com.joyopi.monolith.order.domain.Order;

public record OrderCreateCommand(
        Long userId,
        Long productId,
        Long totalAmount,
        Long pointAmount
) {
    public Order toEntity() {
        return Order.builder()
                .userId(userId)
                .productId(productId)
                .totalAmount(totalAmount)
                .pointAmount(pointAmount)
                .build();
    }
}
