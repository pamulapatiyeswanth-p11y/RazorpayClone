package com.codingshuttle.razorpay.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResources(DuplicateResourceException ex){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ex.getErrorName(), ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ex.getResourceName().toUpperCase()+ "_NOT_FOUND ",
                        ex.getMessage(),
                        LocalDateTime.now()));
    }
}
