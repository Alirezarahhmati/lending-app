package com.lending.app.application.service.impl;

import com.lending.app.application.service.LoanTransactionService;
import com.lending.app.model.entity.LoanTransaction;
import com.lending.app.repository.LoanTransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class LoanTransactionServiceImpl implements LoanTransactionService {

    private final LoanTransactionRepository repository;

    public LoanTransactionServiceImpl(LoanTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public LoanTransaction save(LoanTransaction loanTransaction) {
        return repository.save(loanTransaction);
    }
}
