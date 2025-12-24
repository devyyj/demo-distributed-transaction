package com.example.pointservice.dto;

public record OrderFailedEvent(Long userId, int pointAmount) {
}
