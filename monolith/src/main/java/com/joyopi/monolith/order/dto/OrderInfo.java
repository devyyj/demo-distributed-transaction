package com.joyopi.monolith.order.dto;

import com.joyopi.monolith.order.domain.Order;

public record OrderInfo(
        Long id,
        Long userId,
        Long productId,
        Long totalAmount,
        Long pointAmount,
        Long paymentAmount
) {
    public static OrderInfo from(Order order) {
        return new OrderInfo(
                order.getId(),
                order.getUserId(),
                order.getProductId(),
                order.getTotalAmount(),
                order.getPointAmount(),
                order.getPaymentAmount()
        );
    }
}
