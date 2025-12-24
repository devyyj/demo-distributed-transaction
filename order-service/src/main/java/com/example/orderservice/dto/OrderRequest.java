package com.example.orderservice.dto;

public record OrderRequest(Long userId, int totalAmount, int pointAmount) {
}
