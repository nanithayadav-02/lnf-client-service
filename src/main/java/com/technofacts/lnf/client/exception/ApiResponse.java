package com.technofacts.lnf.client.exception;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    private boolean error;
    private Date timestamp;
    private String statusCode;
    private String statusMessage;
    private String stackTrace;
    private String apiDetails;
}
