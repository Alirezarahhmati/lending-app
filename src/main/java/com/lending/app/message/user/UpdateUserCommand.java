package com.lending.app.message.user;

public record UpdateUserCommand(
        String username,
        String password,
        String email,
        Integer score
) {}


