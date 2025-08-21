package com.lending.app.service.impl;

import com.lending.app.exception.NotFoundException;
import com.lending.app.mapper.LoanMapper;
import com.lending.app.model.entity.Loan;
import com.lending.app.model.record.loan.LoanMessage;
import com.lending.app.model.record.loan.SaveLoanCommand;
import com.lending.app.model.record.loan.UpdateLoanCommand;
import com.lending.app.repository.LoanRepository;
import com.lending.app.service.LoanService;
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
        Loan saved = loanRepository.save(loanMapper.toEntity(command));
        return loanMapper.toMessage(saved);
    }

    @Override
    public LoanMessage update(UpdateLoanCommand command) {
        // todo: if any user has this loan what should happening in case of update?
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
        return loanRepository.findById(id)
                .map(loanMapper::toMessage)
                .orElseThrow(() -> new NotFoundException("Loan"));
    }

    @Override
    public List<LoanMessage> getAll() {
        return loanRepository.findAll().stream().map(loanMapper::toMessage).toList();
    }
}



