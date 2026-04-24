package com.joyopi.monolith.point.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 포인트 사용 요청 DTO입니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointUseCommand {
    private Long userId;
    private Long amount;
}
