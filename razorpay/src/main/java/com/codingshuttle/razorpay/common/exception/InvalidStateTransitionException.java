package com.codingshuttle.razorpay.common.exception;

public class InvalidStateTransitionException extends RuntimeException {
    private final String currentState;
    private final String attemptedEvent;

    public InvalidStateTransitionException(String currentState, String attemptedEvent) {
        super("Invalid transition from state: " + currentState + " with event: " + attemptedEvent);
        this.currentState = currentState;
        this.attemptedEvent = attemptedEvent;
    }
}
