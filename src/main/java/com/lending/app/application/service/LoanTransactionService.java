package com.lending.app.application.service;

import com.lending.app.model.entity.LoanTransaction;

public interface LoanTransactionService {
    LoanTransaction saveAndFlush(LoanTransaction loanTransaction);
    LoanTransaction findById(String id);
}
