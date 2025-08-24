package com.lending.app.model.record.loan;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateLoanCommand(
        @NotBlank String id,
        String name,
        @Min(1) Long amount,
        @Min(1) Integer numberOfInstallments,
        @Min(0) Integer requiredScore,
        @Min(0) Integer awardScore
) {}



