package com.lending.app.record.user;

import java.util.UUID;

public record UserMessage(
        UUID id,
        String username,
        String email,
        int score
) {}


