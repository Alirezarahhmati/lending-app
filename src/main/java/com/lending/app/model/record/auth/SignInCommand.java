package com.lending.app.model.record.auth;

public record SignInCommand(
        String username,
        String password
) {}
