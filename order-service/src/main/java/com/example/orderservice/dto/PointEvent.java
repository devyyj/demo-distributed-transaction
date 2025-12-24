package com.example.orderservice.dto;

public record PointEvent(Long orderId, Long userId, int amount) {}