package com.technofacts.lnf.client.exception;

public final class LnFEntityNotFoundException extends LnFException {

    public LnFEntityNotFoundException(String message) {
        super(LnFExceptionType.ENTITY_NOT_FOUND_EXCEPTION, message);
    }

}
