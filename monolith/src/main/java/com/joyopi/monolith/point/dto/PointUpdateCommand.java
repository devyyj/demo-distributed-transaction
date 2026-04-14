package com.joyopi.monolith.point.dto;

import com.joyopi.monolith.point.domain.Point;

public record PointUpdateCommand(
        Long userId,
        Long pointAmount
) {
    public Point toEntity() {
        return Point.builder()
                .userId(userId)
                .balance(pointAmount)
                .build();
    }
}
