package com.codingshuttle.razorpay.common.enums;

public enum PaymentStatus {
    CREATED,
    AUTHORIZING,
    AUTHORIZED,
    CAPTURING,
    CAPTURED,
    REFUNDED,
    FAILED,
    CANCELLED,
    PARTIALLY_REFUNDED,
    SETTLED,
    AUTH_EXPIRED
}
