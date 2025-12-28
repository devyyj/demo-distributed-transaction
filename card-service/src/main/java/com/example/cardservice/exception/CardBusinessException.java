package com.example.cardservice.exception;

public class CardBusinessException extends RuntimeException {
    public CardBusinessException(String message) {
        super(message);
    }
}
