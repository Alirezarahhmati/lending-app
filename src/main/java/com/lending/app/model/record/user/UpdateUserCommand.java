package com.lending.app.model.record.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserCommand(
        @Size(min = 3, max = 50) String username,
        @Size(min = 6, max = 100) String password,
        @Email @Size(max = 255) String email,
        @Min(0) Integer score
) {}


