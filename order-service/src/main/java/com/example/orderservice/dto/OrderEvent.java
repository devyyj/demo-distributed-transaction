package com.example.orderservice.dto;

public record OrderEvent(Long orderId, Long userId, int pointAmount) {
}
