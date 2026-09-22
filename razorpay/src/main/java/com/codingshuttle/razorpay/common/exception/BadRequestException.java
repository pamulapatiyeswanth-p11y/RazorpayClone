package com.codingshuttle.razorpay.common.exception;

public class BadRequestException extends RuntimeException {
    public final String errorCode;
    public BadRequestException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
