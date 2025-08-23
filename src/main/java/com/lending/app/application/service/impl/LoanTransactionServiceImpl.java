package com.lending.app.application.service.impl;

import com.lending.app.application.service.LoanTransactionService;
import com.lending.app.exception.NotFoundException;
import com.lending.app.model.entity.LoanTransaction;
import com.lending.app.repository.LoanTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoanTransactionServiceImpl implements LoanTransactionService {

    private final LoanTransactionRepository repository;

    public LoanTransactionServiceImpl(LoanTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveAndFlush(LoanTransaction loanTransaction) {
        log.debug("Saving and flushing LoanTransaction for loanId: {}", loanTransaction.getLoan().getId());
        repository.saveAndFlush(loanTransaction);
        log.info("LoanTransaction saved and flushed with id: {}", loanTransaction.getId());
    }

    @Override
    public LoanTransaction findById(String id) {
        log.debug("Fetching LoanTransaction with id: {}", id);
        LoanTransaction transaction = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("LoanTransaction not found with id: {}", id);
                    return new NotFoundException("LoanTransaction");
                });
        log.info("LoanTransaction fetched with id: {}", transaction.getId());
        return transaction;
    }
}
