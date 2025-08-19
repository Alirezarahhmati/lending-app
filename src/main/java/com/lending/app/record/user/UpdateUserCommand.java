package com.lending.app.record.user;

public record UpdateUserCommand(
        String username,
        String password,
        String email,
        Integer score
) {}


