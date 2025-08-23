package com.lending.app.application.processor;

import com.lending.app.application.service.InstallmentService;
import com.lending.app.application.service.LoanTransactionService;
import com.lending.app.exception.NotFoundException;
import com.lending.app.model.entity.Installment;
import com.lending.app.model.entity.LoanTransaction;
import com.lending.app.model.record.loan.LoanTransactionMessage;
import com.lending.app.model.record.loan.LoanApplicationMessage;
import com.lending.app.model.record.loan.LoanInstallmentCommand;
import com.lending.app.util.calculatorUtils;
import com.lending.app.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
public class InstallmentPaymentProcessor {

    private final LoanTransactionService loanTransactionService;
    private final InstallmentService installmentService;
    private final InstallmentAsyncProcessor installmentAsyncProcessor;

    public InstallmentPaymentProcessor(
            LoanTransactionService loanTransactionService, InstallmentService installmentService,
            InstallmentAsyncProcessor installmentAsyncProcessor
    ) {
        this.loanTransactionService = loanTransactionService;
        this.installmentService = installmentService;
        this.installmentAsyncProcessor = installmentAsyncProcessor;
    }

    @Transactional
    public LoanApplicationMessage process(LoanInstallmentCommand installmentCommand) {
        log.debug("Processing installment payment for loanTransactionId: {}", installmentCommand.loanTransactionId());

        Installment installment = installmentService.findNotPaidInstallmentByLoanTransactionId(installmentCommand.loanTransactionId());
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (!Objects.equals(installment.getLoanTransaction().getBorrower().getId(), currentUserId)) {
            log.warn("Installment payment failed: user {} not authorized for loanTransactionId {}", currentUserId, installmentCommand.loanTransactionId());
            throw new NotFoundException("Loan");
        }

        installment.setPaymentDate(LocalDateTime.now());
        installment.setPaid(true);
        Installment savedInstallment = installmentService.saveAndFlush(installment);
        log.info("Installment paid and saved with id: {}", savedInstallment.getId());

        LoanTransaction loanTransaction = installment.getLoanTransaction();
        long paidAmount = loanTransaction.getPaidAmount() + loanTransaction.getLoan().getEachInstallmentAmount();
        loanTransaction.setPaidAmount(paidAmount);

        boolean isEnd = paidAmount >= calculatorUtils.calculateMustPaidAmount(loanTransaction.getLoan().getAmount(), loanTransaction.getLoan().getNumberOfInstallments());
        if (isEnd) {
            loanTransaction.setEndDate(LocalDateTime.now());
            log.info("LoanTransaction {} fully paid and marked as ended", loanTransaction.getId());
        } else {
            log.debug("LoanTransaction {} paid amount updated: {}", loanTransaction.getId(), paidAmount);
        }

        loanTransactionService.saveAndFlush(loanTransaction);

        if (!isEnd) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.debug("Triggering async installment creation for loanTransactionId: {}", loanTransaction.getId());
                    installmentAsyncProcessor.handleInstallmentCreation(new LoanTransactionMessage(loanTransaction.getId()));
                }
            });
        }

        installmentAsyncProcessor.processInstallmentBonus(savedInstallment);
        log.info("Installment bonus processed for installmentId: {}", savedInstallment.getId());

        return new LoanApplicationMessage("Installment paid successfully.");
    }
}