package com.technofacts.lnf.exception;

import java.util.Date;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@RestController
public class LnFExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${printStackTrace:false}")
    private boolean printStackTrace;

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleAllExceptions(Exception exception, WebRequest request) {
        ApiResponse apiResponse = buildExceptionResponse(exception, request, LnFExceptionType.UNKNOWN_EXCEPTION);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    @ExceptionHandler(LnFEntityNotFoundException.class)
    public final ResponseEntity<Object> handleEntityNotFoundException(LnFEntityNotFoundException exception, WebRequest request) {
        ApiResponse apiResponse = buildLnFExceptionResponse(exception, request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
    }

    @ExceptionHandler(LnFBadRequestException.class)
    public final ResponseEntity<Object> handleBadRequestException(LnFBadRequestException exception, WebRequest request) {
        ApiResponse apiResponse = buildLnFExceptionResponse(exception, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    @ExceptionHandler(LnFServiceUnavailableException.class)
    public final ResponseEntity<Object> handleServiceUnavailableException(LnFServiceUnavailableException exception, WebRequest request) {
        ApiResponse apiResponse = buildLnFExceptionResponse(exception, request);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(apiResponse);
    }

    private ApiResponse buildLnFExceptionResponse(LnFException exception,
                                                  WebRequest request) {
        return buildExceptionResponse(exception, request, exception.getExceptionType());
    }

    private ApiResponse buildExceptionResponse(Exception exception,
                                               WebRequest request,
                                               LnFExceptionType exceptionType) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setError(true);
        apiResponse.setTimestamp(new Date());
        apiResponse.setStatusCode(exceptionType.getCode());
        String statusMessage = (exceptionType == LnFExceptionType.UNKNOWN_EXCEPTION) ? "Unknown error occurred" : exception.getMessage();
        apiResponse.setStatusMessage(statusMessage);
        apiResponse.setApiDetails(request.getDescription(false));
        if (printStackTrace) {
            apiResponse.setStackTrace(ExceptionUtils.getStackTrace(exception));
        }
        return apiResponse;
    }
}
