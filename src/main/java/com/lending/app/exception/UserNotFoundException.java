package com.lending.app.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException forId(UUID id) {
        return new UserNotFoundException("User not found: " + id);
    }
}


