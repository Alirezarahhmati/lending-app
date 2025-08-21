package com.lending.app.message.auth;

public record SignUpCommand(
        String username,
        String password,
        String email
) {}
