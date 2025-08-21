package com.lending.app.model.record.user;

public record UserMessage(
        String id,
        String username,
        String email,
        int score
) {}


