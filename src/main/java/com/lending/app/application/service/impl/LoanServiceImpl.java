package com.lending.app.application.service.impl;

import com.lending.app.exception.NotFoundException;
import com.lending.app.mapper.LoanMapper;
import com.lending.app.model.entity.Loan;
import com.lending.app.model.record.loan.LoanMessage;
import com.lending.app.model.record.loan.SaveLoanCommand;
import com.lending.app.model.record.loan.UpdateLoanCommand;
import com.lending.app.repository.LoanRepository;
import com.lending.app.application.service.LoanService;
import com.lending.app.util.calculatorUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;

    public LoanServiceImpl(LoanRepository loanRepository, LoanMapper loanMapper) {
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
    }

    @Override
    public LoanMessage save(SaveLoanCommand command) {
        Loan loan = loanMapper.toEntity(command);
        loan.setEachInstallmentAmount(calculatorUtils.calculateEachInstallmentAmount(command.amount(), command.numberOfInstallments()));
        Loan saved = loanRepository.save(loan);
        return loanMapper.toMessage(saved);
    }

    @Override
    public LoanMessage update(UpdateLoanCommand command) {
        Loan existing = loanRepository.findById(command.id())
                .orElseThrow(() -> new NotFoundException("Loan"));
        loanMapper.apply(command, existing);
        Loan saved = loanRepository.save(existing);
        return loanMapper.toMessage(saved);
    }

    @Override
    public void delete(String id) {
        if (!loanRepository.existsById(id)) {
            throw new NotFoundException("Loan");
        }
        loanRepository.softDeleteById(id);
    }

    @Override
    public LoanMessage get(String id) {
        return loanMapper.toMessage(getLoan(id));
    }

    @Override
    public Loan getLoan(String id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loan"));
    }

    @Override
    public List<LoanMessage> getAll() {
        return loanRepository.findAll().stream().map(loanMapper::toMessage).toList();
    }
}



