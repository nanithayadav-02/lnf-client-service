package com.technofacts.lnf.exception;

public enum LnFExceptionType {

    UNKNOWN_EXCEPTION("LNF-ERR-0000"),
    ENTITY_NOT_FOUND_EXCEPTION("LNF-ERR-0001"),
    INVALID_REQUEST_EXCEPTION("LNF-ERR-0002"),
    SERVICE_UNAVAILABLE_EXCEPTION("LNF-ERR-0003");

    private final String code;

    public String getCode() { return code; }

    LnFExceptionType(String code) { this.code = code; }
}
