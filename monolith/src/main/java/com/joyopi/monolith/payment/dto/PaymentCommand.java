package com.joyopi.monolith.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 요청 DTO입니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCommand {
    private Long orderId;
    private Long amount;
}
