package com.lending.app.exception;

import java.time.Instant;

public record ErrorMessage(
        Instant timestamp,
        String message
) {}