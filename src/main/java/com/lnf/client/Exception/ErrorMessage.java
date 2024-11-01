package com.lnf.client.Exception;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ErrorMessage {

    private int statusCode;
    private Date timestamp;
    private String message;
    private String trace;
}
