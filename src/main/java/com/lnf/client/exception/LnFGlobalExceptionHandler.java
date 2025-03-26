package com.lnf.client.exception;

import com.lnf.exception.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
import java.util.stream.Collectors;

@RestControllerAdvice
public class LnFGlobalExceptionHandler {

    // Handle all LnF custom exceptions
    @ExceptionHandler(LnFEntityNotFoundException.class)
    public ResponseEntity<ApiResponse> handleEntityNotFound(LnFEntityNotFoundException ex, WebRequest request) {
        return buildResponse(ex, HttpStatus.NOT_FOUND, request, false);
    }

    @ExceptionHandler(LnFBadRequestException.class)
    public ResponseEntity<ApiResponse> handleBadRequest(LnFBadRequestException ex, WebRequest request) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST, request, false);
    }

    @ExceptionHandler(LnFServiceUnavailableException.class)
    public ResponseEntity<ApiResponse> handleServiceUnavailable(LnFServiceUnavailableException ex, WebRequest request) {
        return buildResponse(ex, HttpStatus.SERVICE_UNAVAILABLE, request, false);
    }

    @ExceptionHandler(LnFException.class)
    public ResponseEntity<ApiResponse> handleGenericLnFException(LnFException ex, WebRequest request) {
        return buildResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request, true);
    }

    // Handle persistence layer exceptions
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        String rootMsg = ex.getRootCause() != null ?
                ex.getRootCause().getMessage() : ex.getMessage();
        LnFBadRequestException lnfEx = new LnFBadRequestException(
                "Data integrity violation: " + rootMsg);
        return buildResponse(lnfEx, HttpStatus.BAD_REQUEST, request, false);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        LnFBadRequestException lnfEx = new LnFBadRequestException(
                "Database constraint violation: " + ex.getMessage());
        return buildResponse(lnfEx, HttpStatus.BAD_REQUEST, request, false);
    }

    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<ApiResponse> handleJpaSystemException(JpaSystemException ex, WebRequest request) {
        LnFException lnfEx = new LnFException(
                "Persistence error: " + ex.getMostSpecificCause().getMessage(), ex);
        return buildResponse(lnfEx, HttpStatus.INTERNAL_SERVER_ERROR, request, true);
    }

    // Handle validation exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        LnFBadRequestException badRequestEx = new LnFBadRequestException(
                "Validation failed: " + errorMessage);
        return buildResponse(badRequestEx, HttpStatus.BAD_REQUEST, request, false);
    }

    // Generic exception handler as fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex, WebRequest request) {
        LnFException lnfEx = new LnFException(
                "An unexpected error occurred: " + ex.getMessage(), ex);
        return buildResponse(lnfEx, HttpStatus.INTERNAL_SERVER_ERROR, request, true);
    }

    private ResponseEntity<ApiResponse> buildResponse(LnFException ex, HttpStatus status, WebRequest request, boolean includeStackTrace) {
        ApiResponse response = new ApiResponse();
        response.setError(true);
        response.setTimestamp(new Date());
        response.setStatusCode(ex.getExceptionType().getCode());
        response.setStatusMessage(ex.getMessage());
        response.setApiDetails(request.getDescription(false));

        if (includeStackTrace) {
            response.setStackTrace(stackTraceToString(ex));
        }

        return new ResponseEntity<>(response, status);
    }

    private String stackTraceToString(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}