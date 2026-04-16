package com.joyopi.monolith.common.exception;

public class PaymentProcessException extends RuntimeException {

    public PaymentProcessException(Long orderId, String reason) {
        super("결제 처리 실패 - orderId: " + orderId + ", 사유: " + reason);
    }
}
