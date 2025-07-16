package com.pacsdcm4che.pacsdcm4che_be.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException ex) {
        return "Invalid argument exception: " + ex.getMessage();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleResourceNotFoundException(ResourceNotFoundException ex) {
        return "ResourceNotFound Exception: " + ex.getMessage();
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusinessException(BusinessException ex) {
        return "Business Exception: " + ex.getMessage();
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleRuntimeException(Throwable ex) {
        ex.printStackTrace();
        return "Internal server error";
    }
}
