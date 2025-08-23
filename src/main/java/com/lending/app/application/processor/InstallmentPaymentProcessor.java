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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.lending.app.config.RabbitConfig.INSTALLMENT_QUEUE;

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
        Installment installment = installmentService.findNotPaidInstallmentByLoanTransactionId(installmentCommand.loanTransactionId());
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (!Objects.equals(installment.getLoanTransaction().getBorrower().getId(),currentUserId)) {
            throw new NotFoundException("Loan");
        }

        installment.setPaymentDate(LocalDateTime.now());
        installment.setPaid(true);
        Installment savedInstallment = installmentService.saveAndFlush(installment);

        LoanTransaction loanTransaction = installment.getLoanTransaction();
        long paidAmount = loanTransaction.getPaidAmount() + loanTransaction.getLoan().getEachInstallmentAmount();
        loanTransaction.setPaidAmount(paidAmount);
        boolean isEnd = paidAmount >= calculatorUtils.calculateMustPaidAmount(loanTransaction.getLoan().getAmount(), loanTransaction.getLoan().getNumberOfInstallments());
        if (isEnd)
            loanTransaction.setEndDate(LocalDateTime.now());
        loanTransactionService.saveAndFlush(loanTransaction);

        if (!isEnd)
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    installmentAsyncProcessor.handleInstallmentCreation(new LoanTransactionMessage(loanTransaction.getId()));
                }
            });

        installmentAsyncProcessor.processInstallmentBonus(savedInstallment);
        return new LoanApplicationMessage("Installment paid successfully.");
    }
}
