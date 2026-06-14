package com.codingshuttle.razorpay.common.exception;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException {
    private final String errorName;
    public DuplicateResourceException(String errorName, String message) {
        super(message);
        this.errorName =  errorName;
    }
}
