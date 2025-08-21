package com.lending.app.service;

import com.lending.app.model.record.loan.LoanMessage;
import com.lending.app.model.record.loan.SaveLoanCommand;
import com.lending.app.model.record.loan.UpdateLoanCommand;

import java.util.List;

public interface LoanService {
    LoanMessage save(SaveLoanCommand command);
    LoanMessage update(UpdateLoanCommand command);
    void delete(String id);
    LoanMessage get(String id);
    List<LoanMessage> getAll();
}



