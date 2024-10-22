/*
 * Copyright (c)  Lever And Fulcrum (LNF)
 */
package com.lnf.client.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private HttpStatus statuses;
    private List<Message> messages;
}

