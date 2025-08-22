package com.lending.app.application.service;

import com.lending.app.model.entity.Installment;

public interface InstallmentService {
    Installment save(Installment installment);
    Integer installmentCountByLoanTransactionId(String loanTransactionId);
    Installment findNotPaidInstallmentByLoanTransactionId(String loanTransactionId);
}
