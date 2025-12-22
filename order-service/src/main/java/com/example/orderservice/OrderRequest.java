package com.example.orderservice;

public record OrderRequest(Long userId, int totalAmount, int pointAmount) {
}
