package com.lending.app.model.record.user;

public record CreateUserCommand(
        String username,
        String password,
        String email,
        int score
) {}


