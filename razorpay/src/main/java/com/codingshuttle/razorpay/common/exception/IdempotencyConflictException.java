package com.codingshuttle.razorpay.common.exception;

public class IdempotencyConflictException extends RuntimeException {
    public final String errorCode;
    public IdempotencyConflictException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
