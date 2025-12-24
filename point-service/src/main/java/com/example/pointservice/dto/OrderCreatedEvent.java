package com.example.pointservice.dto;

public record OrderCreatedEvent(Long orderId, Long userId, int pointAmount, int cardAmount) {
}
