package com.lending.app.message.auth;

public record SignInCommand(
        String username,
        String password
) {}
