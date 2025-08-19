package com.lending.app.record.user;

public record CreateUserCommand(
        String username,
        String password,
        String email,
        int score
) {}


