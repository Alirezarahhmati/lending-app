package com.lending.app.model.record.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.lending.app.model.enums.Role;

public record CreateUserCommand(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 6, max = 100) String password,
        @NotBlank @Email @Size(max = 255) String email,
        @Min(0) int score,
        Role role
) {}


