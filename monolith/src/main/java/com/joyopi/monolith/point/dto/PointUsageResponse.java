package com.joyopi.monolith.point.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 포인트 사용 결과 응답 DTO입니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointUsageResponse {
    private Long userId;
    private Long remainingBalance;
}
