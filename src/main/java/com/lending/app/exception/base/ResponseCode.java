package com.lending.app.exception.base;

import lombok.Getter;

@Getter
public enum ResponseCode {

    // Success
    SUCCESS(0, "Success"),

    // Client errors
    VALIDATION_EXCEPTION(-1, "Validation exception"),
    ALREADY_EXISTS_EXCEPTION(-2, "%s already exists"),
    NOT_FOUND_EXCEPTION(-3, "%s not found"),
    AUTHENTICATION_EXCEPTION(-3, "Authentication exception"),

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
