package com.joyopi.monolith.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 생성을 위한 서비스 레이어 전용 Command 객체입니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateCommand {
    private Long userId;
    private Long productId;
    private Long totalAmount;
    private Long pointAmount;
}
