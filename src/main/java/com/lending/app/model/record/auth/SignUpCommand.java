package com.lending.app.model.record.auth;

public record SignUpCommand(
        String username,
        String password,
        String email
) {}
