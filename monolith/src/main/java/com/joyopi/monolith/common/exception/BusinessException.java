package com.joyopi.monolith.common.exception;

/**
 * 프로젝트 전체에서 공통으로 사용할 비즈니스 예외 클래스입니다.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
