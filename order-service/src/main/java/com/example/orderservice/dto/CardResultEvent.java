package com.example.orderservice.dto;

public record CardResultEvent(Long orderId, Long userId, int amount, String status) {}