package com.lending.app.application.processor;

import com.lending.app.application.service.LoanService;
import com.lending.app.application.service.LoanTransactionService;
import com.lending.app.application.service.UserService;
import com.lending.app.exception.InsufficientScoreException;
import com.lending.app.model.entity.Loan;
import com.lending.app.model.entity.LoanTransaction;
import com.lending.app.model.entity.User;
import com.lending.app.model.record.loan.LoanApplicationCommand;
import com.lending.app.model.record.loan.LoanApplicationMessage;
import com.lending.app.model.record.loan.LoanTransactionMessage;
import com.lending.app.util.calculatorUtils;
import com.lending.app.util.SecurityUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Service
public class LoanApplicationProcessor {

    private final LoanTransactionService loanTransactionService;
    private final UserService userService;
    private final LoanService loanService;
    private final InstallmentAsyncProcessor installmentAsyncProcessor;

    public LoanApplicationProcessor(
            LoanTransactionService loanTransactionService,
            UserService userService,
            LoanService loanService, InstallmentAsyncProcessor installmentAsyncProcessor
    ) {
        this.loanTransactionService = loanTransactionService;
        this.userService = userService;
        this.loanService = loanService;
        this.installmentAsyncProcessor = installmentAsyncProcessor;
    }

    @Transactional
    public LoanApplicationMessage process(LoanApplicationCommand application) {
        String borrowerId = SecurityUtils.getCurrentUserId();
        User borrower = userService.getUserForUpdate(borrowerId);
        Loan loan = loanService.getLoan(application.loanId());

        LoanTransaction transaction = initializeTransaction(loan, borrower);

        LoanApplicationMessage result = borrower.getScore() >= loan.getRequiredScore()
                ? handleBorrower(borrower, loan, transaction)
                : handleWithGuarantor(application, borrower, loan, transaction);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                installmentAsyncProcessor.handleInstallmentCreation(new LoanTransactionMessage(transaction.getId()));
            }
        });

        return result;
    }

    private LoanTransaction initializeTransaction(Loan loan, User borrower) {
        LoanTransaction transaction = new LoanTransaction();
        transaction.setLoan(loan);
        transaction.setBorrower(borrower);
        transaction.setStartDate(LocalDateTime.now());
        return transaction;
    }

    private LoanApplicationMessage handleBorrower(User borrower, Loan loan, LoanTransaction transaction) {
        loanTransactionService.saveAndFlush(transaction);
        userService.changeScore(borrower, -loan.getRequiredScore());
        return successMessage();
    }

    private LoanApplicationMessage handleWithGuarantor(LoanApplicationCommand application, User borrower, Loan loan, LoanTransaction transaction) {
        if (application.guarantorId() == null || application.guarantorId().isBlank()) {
            throw new InsufficientScoreException();
        }

        User guarantor = userService.getUserForUpdate(application.guarantorId());
        int neededFromGuarantor = calculatorUtils.calculateGuarantorScore(loan);
        int neededFromBorrower = loan.getRequiredScore() - neededFromGuarantor;

        if (guarantor.getScore() < neededFromGuarantor) {
            throw new InsufficientScoreException();
        }

        userService.changeScore(borrower, -neededFromBorrower);
        userService.changeScore(guarantor, -neededFromGuarantor);

        transaction.setGuarantor(guarantor);
        loanTransactionService.saveAndFlush(transaction);
        return successMessage();
    }

    private LoanApplicationMessage successMessage() {
        return new LoanApplicationMessage("Your loan application has been successfully submitted.");
    }

}