package com.technofacts.lnf.client.exception;

import java.util.function.Predicate;

public final class LnFBadRequestException extends LnFException {

    public LnFBadRequestException(String message) {
        super(LnFExceptionType.INVALID_REQUEST_EXCEPTION, message);
    }

    public static <T> void throwOnCondition(Predicate<T> predicate, T toTest, String message) {
        if (predicate.test(toTest)) {
            throw new LnFBadRequestException(message);
        }
    }
}
