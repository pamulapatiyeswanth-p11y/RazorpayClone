package com.codingshuttle.razorpay.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.validation.FieldError;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL) // only includes not null values in the response
public record ErrorResponse(
        String errorCode,
        String errorDescription,
        LocalDateTime timeStamp,
        List<FieldError> fieldErrors


) {
    public record FieldError(String field, String message){}

    public static ErrorResponse of(String errorCode,String errorDescription,LocalDateTime timeStamp){
        return new ErrorResponse(errorCode,errorDescription,timeStamp,null);
    }

    public static ErrorResponse of(String errorCode,String errorDescription, LocalDateTime timeStamp,List<FieldError> fieldErrors){
        return new ErrorResponse(errorCode,errorDescription,timeStamp,fieldErrors);
    }
}
