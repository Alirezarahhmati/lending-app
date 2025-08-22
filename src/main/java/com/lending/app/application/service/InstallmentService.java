package com.lending.app.application.service;

import com.lending.app.model.entity.Installment;

public interface InstallmentService {
    Installment save(Installment installment);
    Installment saveAndFlush(Installment installment);
    Installment findNotPaidInstallmentByLoanTransactionId(String loanTransactionId);
}
