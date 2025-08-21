package com.lending.app.record.auth;

public record SignInCommand(
        String username,
        String password
) {}
