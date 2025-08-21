package com.lending.app.record.auth;

public record SignUpCommand(
        String username,
        String password,
        String email
) {}
