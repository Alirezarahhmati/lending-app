package com.lending.app.application.service;

import com.lending.app.model.entity.LoanTransaction;
import com.lending.app.model.record.loan.UserLoanTransactionMessage;

import java.util.List;

public interface LoanTransactionService {
    LoanTransaction saveAndFlush(LoanTransaction loanTransaction);
    LoanTransaction findById(String id);
    List<UserLoanTransactionMessage> findUserLoanTransactionsByUserId();
}
