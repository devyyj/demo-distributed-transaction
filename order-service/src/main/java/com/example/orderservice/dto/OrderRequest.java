package com.example.orderservice.dto;

public record OrderRequest(String requestId, Long userId, int totalAmount, int pointAmount) {
}
