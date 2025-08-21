package com.lending.app.message.user;

public record CreateUserCommand(
        String username,
        String password,
        String email,
        int score
) {}


