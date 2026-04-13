package com.joyopi.monolith.order.dto;

public record OrderRequest(
        Long userId,
        Long productId,
        Long totalAmount,
        Long pointAmount
) {
}
