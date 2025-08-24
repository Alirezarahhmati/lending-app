package com.lending.app.application.service.impl;

import com.lending.app.application.service.InstallmentService;
import com.lending.app.exception.NotFoundException;
import com.lending.app.model.entity.Installment;
import com.lending.app.repository.InstallmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class InstallmentServiceImpl implements InstallmentService {

    private final InstallmentRepository installmentRepository;

    public InstallmentServiceImpl(InstallmentRepository installmentRepository) {
        this.installmentRepository = installmentRepository;
    }

    @Transactional
    @Override
    public Installment save(Installment installment) {
        log.debug("Saving installment for loanTransactionId: {}", installment.getLoanTransaction().getId());
        Installment saved = installmentRepository.save(installment);
        log.info("Installment saved with id: {}", saved.getId());
        return saved;
    }

    @Transactional
    @Override
    public Installment saveAndFlush(Installment installment) {
        log.debug("Saving and flushing installment for loanTransactionId: {}", installment.getLoanTransaction().getId());
        Installment saved = installmentRepository.saveAndFlush(installment);
        log.info("Installment saved and flushed with id: {}", saved.getId());
        return saved;
    }

    @Override
    public Installment findNotPaidInstallmentByLoanTransactionId(String loanTransactionId) {
        log.debug("Finding not-paid installment for loanTransactionId: {}", loanTransactionId);
        Installment installment = installmentRepository.findLastByLoanTransactionId(loanTransactionId)
                .orElseThrow(() -> {
                    log.warn("No not-paid installment found for loanTransactionId: {}", loanTransactionId);
                    return new NotFoundException("Installment");
                });
        log.info("Found not-paid installment with id: {}", installment.getId());
        return installment;
    }
}