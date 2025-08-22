package com.lending.app.model.record.loan;

import jakarta.validation.constraints.NotBlank;

public record LoanInstallmentCommand (
        @NotBlank String loanTransactionId
){}
