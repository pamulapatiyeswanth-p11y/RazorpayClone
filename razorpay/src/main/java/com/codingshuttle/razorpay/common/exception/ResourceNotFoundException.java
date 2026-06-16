package com.codingshuttle.razorpay.common.exception;

import jakarta.persistence.GeneratedValue;
import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final Object identifier;
    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(resourceName + " no found: "+ identifier);
        this.resourceName = resourceName;
        this.identifier = identifier;
    }
}
