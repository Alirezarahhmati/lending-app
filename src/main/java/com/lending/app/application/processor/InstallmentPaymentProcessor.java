package com.lending.app.application.processor;

import com.lending.app.application.service.InstallmentService;
import com.lending.app.application.service.LoanTransactionService;
import com.lending.app.exception.InsufficientException;
import com.lending.app.exception.NotFoundException;
import com.lending.app.model.entity.Installment;
import com.lending.app.model.entity.LoanTransaction;
import com.lending.app.model.record.loan.LoanApplicationMessage;
import com.lending.app.model.record.loan.LoanInstallmentCommand;
import com.lending.app.util.LoanUtils;
import com.lending.app.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class InstallmentPaymentProcessor {

    private final LoanTransactionService loanTransactionService;
    private final InstallmentService installmentService;
    private final InstallmentAsyncProcessor installmentAsyncProcessor;

    public InstallmentPaymentProcessor(LoanTransactionService loanTransactionService, InstallmentService installmentService, InstallmentAsyncProcessor installmentAsyncProcessor) {
        this.loanTransactionService = loanTransactionService;
        this.installmentService = installmentService;
        this.installmentAsyncProcessor = installmentAsyncProcessor;
    }

    @Transactional
    public LoanApplicationMessage process(LoanInstallmentCommand installmentCommand) {
        Installment installment = installmentService.findNotPaidInstallmentByLoanTransactionId(installmentCommand.loanTransactionId());
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (!Objects.equals(installment.getLoanTransaction().getBorrower().getId(),currentUserId)) {
            throw new NotFoundException("Loan");
        }

        installment.setPaymentDate(LocalDateTime.now());
        installment.setPaid(true);
        Installment savedInstallment = installmentService.save(installment);

        LoanTransaction loanTransaction = installment.getLoanTransaction();
        long paidAmount = loanTransaction.getPaidAmount() + loanTransaction.getLoan().getEachInstallmentAmount();
        loanTransaction.setPaidAmount(paidAmount);
        boolean isEnd = paidAmount >= LoanUtils.calculateMustPaidAmount(loanTransaction.getLoan().getAmount(), loanTransaction.getLoan().getNumberOfInstallments());
        if (isEnd)
            loanTransaction.setEndDate(LocalDateTime.now());
        LoanTransaction savedLoanTransaction = loanTransactionService.save(loanTransaction);

        if (!isEnd)
            installmentAsyncProcessor.createNextInstallment(savedLoanTransaction);
        installmentAsyncProcessor.processInstallmentBonus(savedInstallment);
        return new LoanApplicationMessage("Installment paid successfully.");
    }
}
