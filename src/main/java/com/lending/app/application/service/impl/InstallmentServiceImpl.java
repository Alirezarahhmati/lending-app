package com.lending.app.application.service.impl;

import com.lending.app.application.service.InstallmentService;
import com.lending.app.exception.NotFoundException;
import com.lending.app.model.entity.Installment;
import com.lending.app.repository.InstallmentRepository;
import org.springframework.stereotype.Service;

@Service
public class InstallmentServiceImpl implements InstallmentService {

    private final InstallmentRepository installmentRepository;

    public InstallmentServiceImpl(InstallmentRepository installmentRepository) {
        this.installmentRepository = installmentRepository;
    }

    @Override
    public Installment save(Installment installment) {
        return installmentRepository.save(installment);
    }

    @Override
    public Installment saveAndFlush(Installment installment) {
        return installmentRepository.saveAndFlush(installment);
    }

    public Installment findNotPaidInstallmentByLoanTransactionId(String loanTransactionId) {
        return installmentRepository.findLastByLoanTransactionId(loanTransactionId)
                .orElseThrow(() -> new NotFoundException("Installment"));

    }
}
