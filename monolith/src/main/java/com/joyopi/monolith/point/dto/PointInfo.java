package com.joyopi.monolith.point.dto;

import com.joyopi.monolith.point.domain.Point;

public record PointInfo(
        Long userId,
        Long balance
) {
    public static PointInfo from(Point point) {
        return new PointInfo(point.getUserId(), point.getBalance());
    }
}
