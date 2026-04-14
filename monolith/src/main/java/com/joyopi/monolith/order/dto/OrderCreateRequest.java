package com.joyopi.monolith.order.dto;

public record OrderCreateRequest(
        Long userId,
        Long productId,
        Long totalAmount,
        Long pointAmount
) {
    public OrderCreateCommand toCommand() {
        return new OrderCreateCommand(userId, productId, totalAmount, pointAmount);
    }
}
