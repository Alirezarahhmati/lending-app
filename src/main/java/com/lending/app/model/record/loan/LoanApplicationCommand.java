package com.lending.app.model.record.loan;

public record LoanApplicationCommand (
    String loanId,
    String guarantorId
){}
