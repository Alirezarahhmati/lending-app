package com.lending.app.model.record.loan;

public record LoanMessage(
        String id,
        String name,
        long amount,
        int numberOfInstallments,
        int requiredScore,
        int awardScore
) {}



