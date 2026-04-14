package com.joyopi.monolith.order.dto;

public record OrderResponse(
        Long id
) {
    public static OrderResponse from(OrderInfo orderInfo) {
        return new OrderResponse(orderInfo.id());
    }
}
