/*
 * Copyright (c)  Lever And Fulcrum (LNF)
 */
package com.lnf.client.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String severity;
    private String message;
}
