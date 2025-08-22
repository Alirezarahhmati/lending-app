package com.lending.app.exception.base;

import lombok.Getter;

@Getter
public enum ResponseCode {

    // Success
    SUCCESS(0, "Success"),

    // Client errors
    UNAUTHORIZED(-1, "Unauthorized"),
    VALIDATION_EXCEPTION(-2, "Validation exception"),
    ALREADY_EXISTS_EXCEPTION(-3, "%s already exists"),
    NOT_FOUND_EXCEPTION(-4, "%s not found"),
    AUTHENTICATION_EXCEPTION(-5, "Authentication exception"),
    INSUFFICIENT_SCORE_EXCEPTION(-5, "The available score is not sufficient to complete this loan application. " +
            "Please try again with a guarantor who has enough score or after improving your own score."),
    INSUFFICIENT_EXCEPTION(-6, "Insufficient %s"),

    // Server errors
    INTERNAL_SERVER_ERROR(-100, "Internal server error");

    private final Integer code;
    private final String message;

    ResponseCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public String formatMessage(Object... args) {
        return String.format(this.message, args != null ? args : new Object[]{});
    }
}
