package com.technofacts.lnf.client.util;

import com.technofacts.lnf.client.exception.LnFBadRequestException;
import com.technofacts.lnf.client.exception.LnFEntityNotFoundException;
import org.springframework.http.HttpStatus;

/**
 * Simple static methods to be called at the start of your own methods to verify correct arguments and state. If the Precondition fails, an {@link HttpStatus} code is thrown
 */
public final class RestValidator {

    private RestValidator() {
        throw new AssertionError();
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @return the non-null reference that was validated
     * @throws LnFEntityNotFoundException if {@code reference} is null
     */
    public static <T> T checkNotNull(final T reference) {
        return checkNotNull(reference, null);
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @param message   the message of the exception if the check fails
     * @return the non-null reference that was validated
     * @throws LnFEntityNotFoundException if {@code reference} is null
     */
    public static <T> T checkNotNull(final T reference, final String message) {
        if (reference == null) {
            throw new LnFEntityNotFoundException(message);
        }
        return reference;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @return the non-null reference that was validated
     * @throws LnFBadRequestException if {@code reference} is null
     */
    public static <T> T checkRequestElementNotNull(final T reference) {
        return checkRequestElementNotNull(reference, null);
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @param message   the message of the exception if the check fails
     * @return the non-null reference that was validated
     * @throws LnFBadRequestException if {@code reference} is null
     */
    public static <T> T checkRequestElementNotNull(final T reference, final String message) {
        if (reference == null) {
            throw new LnFBadRequestException(message);
        }
        return reference;
    }

//    /**
//     * Ensures the truth of an expression
//     *
//     * @param expression a boolean expression
//     * @throws LnFConflictException if {@code expression} is false
//     */
//    public static void checkRequestState(final boolean expression) {
//        checkRequestState(expression, null);
//    }

//    /**
//     * Ensures the truth of an expression
//     *
//     * @param expression a boolean expression
//     * @param message    the message of the exception if the check fails
//     * @throws LnFConflictException if {@code expression} is false
//     */
//    public static void checkRequestState(final boolean expression, final String message) {
//        if (!expression) {
//            throw new LnFConflictException(message);
//        }
//    }

}
