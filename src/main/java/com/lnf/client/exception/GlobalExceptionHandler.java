/*
 * Copyright (c)  Lever And Fulcrum (LNF)
 */
package com.lnf.client.exception;

import com.lnf.exception.LnFEntityNotFoundException;
import com.lnf.exception.LnFException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(com.lnf.exception.LnFException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(LnFException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, List.of(
                new Message("error", ex.getMessage()))); // Use your custom Message class
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(com.lnf.exception.LnFEntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomNotFoundException(LnFEntityNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND, List.of(
                new Message("error", ex.getMessage()))); // Use your custom Message class
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


    // Global handler for all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, List.of(
                new Message("warn", "Unexpected error occurred"),
                new Message("error", ex.getMessage())));

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
