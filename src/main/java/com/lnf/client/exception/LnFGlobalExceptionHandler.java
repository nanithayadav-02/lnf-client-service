package com.lnf.client.exception;

import com.lnf.exception.*;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.TransactionException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.naming.AuthenticationException;
import java.util.Date;
import java.util.stream.Collectors;

@RestControllerAdvice
public class LnFGlobalExceptionHandler {

    // Custom LnF Exceptions
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

    // Database & Persistence
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        String rootMsg = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        LnFBadRequestException lnfEx = new LnFBadRequestException("Data integrity violation: " + rootMsg);
        return buildResponse(lnfEx, HttpStatus.BAD_REQUEST, request, false);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        LnFBadRequestException lnfEx = new LnFBadRequestException("Database constraint violation: " + ex.getMessage());
        return buildResponse(lnfEx, HttpStatus.BAD_REQUEST, request, false);
    }

    @ExceptionHandler({JpaSystemException.class, PersistenceException.class})
    public ResponseEntity<ApiResponse> handleJpaExceptions(RuntimeException ex, WebRequest request) {
        LnFException lnfEx = new LnFException("Persistence error: " + ex.getCause().getMessage(), ex);
        return buildResponse(lnfEx, HttpStatus.INTERNAL_SERVER_ERROR, request, true);
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, PessimisticLockingFailureException.class})
    public ResponseEntity<ApiResponse> handleLockingExceptions(RuntimeException ex, WebRequest request) {
        LnFException lnfEx = new LnFException("Concurrency conflict: " + ex.getMessage(), ex);
        return buildResponse(lnfEx, HttpStatus.CONFLICT, request, false);
    }

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ApiResponse> handleTransactionException(TransactionException ex, WebRequest request) {
        LnFException lnfEx = new LnFException("Transaction failed: " + ex.getMessage(), ex);
        return buildResponse(lnfEx, HttpStatus.INTERNAL_SERVER_ERROR, request, true);
    }

    // Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        LnFBadRequestException badRequestEx = new LnFBadRequestException("Validation failed: " + errorMessage);
        return buildResponse(badRequestEx, HttpStatus.BAD_REQUEST, request, false);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse> handleBindException(BindException ex, WebRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        LnFBadRequestException badRequestEx = new LnFBadRequestException("Binding error: " + errorMessage);
        return buildResponse(badRequestEx, HttpStatus.BAD_REQUEST, request, false);
    }

    // Web & HTTP
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        LnFBadRequestException lnfEx = new LnFBadRequestException("HTTP method not allowed: " + ex.getMethod());
        return buildResponse(lnfEx, HttpStatus.METHOD_NOT_ALLOWED, request, false);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex, WebRequest request) {
        LnFBadRequestException lnfEx = new LnFBadRequestException("Unsupported media type: " + ex.getContentType());
        return buildResponse(lnfEx, HttpStatus.UNSUPPORTED_MEDIA_TYPE, request, false);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, WebRequest request) {
        LnFBadRequestException lnfEx = new LnFBadRequestException("File upload size exceeded");
        return buildResponse(lnfEx, HttpStatus.PAYLOAD_TOO_LARGE, request, false);
    }

    // Security
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        LnFException lnfEx = new LnFException("Access denied: " + ex.getMessage(), ex);
        return buildResponse(lnfEx, HttpStatus.FORBIDDEN, request, false);
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse> handleAuthenticationException(RuntimeException ex, WebRequest request) {
        LnFException lnfEx = new LnFException("Authentication failed: " + ex.getMessage(), ex);
        return buildResponse(lnfEx, HttpStatus.UNAUTHORIZED, request, false);
    }

    // Runtime Exceptions
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        LnFBadRequestException lnfEx = new LnFBadRequestException("Invalid argument: " + ex.getMessage());
        return buildResponse(lnfEx, HttpStatus.BAD_REQUEST, request, false);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse> handleIllegalState(IllegalStateException ex, WebRequest request) {
        LnFException lnfEx = new LnFException("Illegal state: " + ex.getMessage(), ex);
        return buildResponse(lnfEx, HttpStatus.INTERNAL_SERVER_ERROR, request, true);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiResponse> handleUnsupportedOperation(UnsupportedOperationException ex, WebRequest request) {
        LnFException lnfEx = new LnFException("Unsupported operation: " + ex.getMessage(), ex);
        return buildResponse(lnfEx, HttpStatus.NOT_IMPLEMENTED, request, true);
    }

    // Fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex, WebRequest request) {
        LnFException lnfEx = new LnFException("An unexpected error occurred: " + ex.getMessage(), ex);
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