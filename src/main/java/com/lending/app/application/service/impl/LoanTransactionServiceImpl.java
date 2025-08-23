package com.lending.app.application.service.impl;

import com.lending.app.application.service.LoanTransactionService;
import com.lending.app.exception.NotFoundException;
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
    public void saveAndFlush(LoanTransaction loanTransaction) {
        repository.saveAndFlush(loanTransaction);
    }

    @Override
    public LoanTransaction findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("LoanTransaction"));
    }
}
