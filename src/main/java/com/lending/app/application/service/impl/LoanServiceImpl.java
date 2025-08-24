package com.lending.app.application.service.impl;

import com.lending.app.exception.NotFoundException;
import com.lending.app.mapper.LoanMapper;
import com.lending.app.model.entity.Loan;
import com.lending.app.model.record.loan.LoanMessage;
import com.lending.app.model.record.loan.LoanMessageSet;
import com.lending.app.model.record.loan.SaveLoanCommand;
import com.lending.app.model.record.loan.UpdateLoanCommand;
import com.lending.app.repository.LoanRepository;
import com.lending.app.application.service.LoanService;
import com.lending.app.util.CalculatorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;

    public LoanServiceImpl(LoanRepository loanRepository, LoanMapper loanMapper) {
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
    }

    @Override
    @Transactional
    @CachePut(value = "loans", key = "#result.id")
    @CacheEvict(value = "loans_all", allEntries = true)
    public LoanMessage save(SaveLoanCommand command) {
        log.debug("Saving new loan: {}", command);
        Loan loan = loanMapper.toEntity(command);
        loan.setEachInstallmentAmount(CalculatorUtils.calculateEachInstallmentAmount(command.amount(), command.numberOfInstallments()));
        Loan saved = loanRepository.save(loan);
        log.info("Loan saved with id: {}", saved.getId());
        return loanMapper.toMessage(saved);
    }

    @Override
    @Transactional
    @CachePut(value = "loans", key = "#command.id")
    @CacheEvict(value = "loans_all", allEntries = true)
    public LoanMessage update(UpdateLoanCommand command) {
        log.debug("Updating loan with id: {}", command.id());
        Loan existing = loanRepository.findById(command.id())
                .orElseThrow(() -> {
                    log.warn("Loan not found with id: {}", command.id());
                    return new NotFoundException("Loan");
                });
        loanMapper.apply(command, existing);
        Loan saved = loanRepository.save(existing);
        log.info("Loan updated with id: {}", saved.getId());
        return loanMapper.toMessage(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"loans", "loans_all"}, key = "#id")
    public void delete(String id) {
        log.debug("Deleting loan with id: {}", id);
        if (!loanRepository.existsById(id)) {
            log.warn("Loan not found for deletion with id: {}", id);
            throw new NotFoundException("Loan");
        }
        loanRepository.softDeleteById(id);
        log.info("Loan soft-deleted with id: {}", id);
    }

    @Override
    @Cacheable(value = "loans", key = "#id")
    public LoanMessage get(String id) {
        log.debug("Getting loan with id: {}", id);
        return loanMapper.toMessage(getLoan(id));
    }

    @Override
    public Loan getLoan(String id) {
        log.debug("Fetching loan entity with id: {}", id);
        return loanRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Loan not found with id: {}", id);
                    return new NotFoundException("Loan");
                });
    }

    @Override
    @Cacheable(value = "loans_all", key = "#root.methodName")
    public LoanMessageSet getAll() {
        log.debug("Fetching all loans");
        Set<LoanMessage> loans = loanRepository.findAll().stream()
                .map(loanMapper::toMessage)
                .collect(Collectors.toSet());
        log.info("Fetched {} loans", loans.size());
        return new LoanMessageSet(loans);
    }
}