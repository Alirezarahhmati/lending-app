package com.lending.app.application.service.impl;

import com.lending.app.application.service.LoanTransactionService;
import com.lending.app.exception.NotFoundException;
import com.lending.app.mapper.LoanTransactionMapper;
import com.lending.app.model.entity.LoanTransaction;
import com.lending.app.model.record.loan.UserLoanTransactionMessage;
import com.lending.app.repository.LoanTransactionRepository;
import com.lending.app.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class LoanTransactionServiceImpl implements LoanTransactionService {

    private final LoanTransactionRepository repository;
    private final LoanTransactionMapper loanTransactionMapper;

    public LoanTransactionServiceImpl(LoanTransactionRepository repository, LoanTransactionMapper loanTransactionMapper) {
        this.repository = repository;
        this.loanTransactionMapper = loanTransactionMapper;
    }

    @Override
    @Transactional
    public LoanTransaction saveAndFlush(LoanTransaction loanTransaction) {
        log.debug("Saving and flushing LoanTransaction for loanId: {}", loanTransaction.getLoan().getId());
        LoanTransaction saved = repository.saveAndFlush(loanTransaction);
        log.info("LoanTransaction saved and flushed with id: {}", saved.getId());
        return saved;
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

    @Override
    public List<UserLoanTransactionMessage> findUserLoanTransactionsByUserId() {
        String userId = SecurityUtils.getCurrentUserId();
        log.debug("Fetching UserLoanTransactions for userId: {}", userId);

        List<LoanTransaction> transactions = repository.findByUserId(userId);
        List<UserLoanTransactionMessage> userLoanTransactions = transactions.stream()
                .map(loanTransactionMapper::toMessage)
                .collect(java.util.stream.Collectors.toList());
        log.info("Fetched {} UserLoanTransactions for userId: {}", userLoanTransactions.size(), userId);
        return userLoanTransactions;
    }
}