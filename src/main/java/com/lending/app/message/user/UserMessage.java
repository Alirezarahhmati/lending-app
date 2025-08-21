package com.lending.app.message.user;

import java.util.UUID;

public record UserMessage(
        UUID id,
        String username,
        String email,
        int score
) {}


