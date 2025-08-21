package com.lending.app.model.record.loan;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SaveLoanCommand(
        @NotBlank String name,
        @Min(1) long amount,
        @Min(1) int numberOfInstallments,
        @Min(0) int requiredScore,
        @Min(0) int awardScore
) {}


