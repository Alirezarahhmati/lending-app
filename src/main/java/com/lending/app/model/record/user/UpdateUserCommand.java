package com.lending.app.model.record.user;

public record UpdateUserCommand(
        String username,
        String password,
        String email,
        Integer score
) {}


