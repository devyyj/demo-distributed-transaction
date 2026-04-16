package com.joyopi.monolith.common.exception;

public class InsufficientPointException extends RuntimeException {

    public InsufficientPointException(int balance, int requested) {
        super("포인트 잔액이 부족합니다. 잔액: " + balance + ", 요청: " + requested);
    }
}
