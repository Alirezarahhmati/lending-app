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
import com.lending.app.util.calculatorUtils;
import com.lending.app.util.SecurityUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Service
public class LoanApplicationProcessor {

    private final LoanTransactionService loanTransactionService;
    private final InstallmentAsyncProcessor installmentAsyncProcessor;
    private final UserService userService;
    private final LoanService loanService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public LoanApplicationProcessor(
            LoanTransactionService loanTransactionService,
            InstallmentAsyncProcessor installmentAsyncProcessor,
            UserService userService,
            LoanService loanService, ApplicationEventPublisher applicationEventPublisher
    ) {
        this.loanTransactionService = loanTransactionService;
        this.installmentAsyncProcessor = installmentAsyncProcessor;
        this.userService = userService;
        this.loanService = loanService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public LoanApplicationMessage process(LoanApplicationCommand application) {
        String borrowerId = SecurityUtils.getCurrentUserId();
        User borrower = userService.getUserForUpdate(borrowerId);
        Loan loan = loanService.getLoan(application.loanId());

        LoanTransaction transaction = initializeTransaction(loan, borrower);

        return borrower.getScore() >= loan.getRequiredScore()
                ? handleBorrower(borrower, loan, transaction)
                : handleWithGuarantor(application, borrower, loan, transaction);
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
        installmentAsyncProcessor.createNextInstallment(transaction);
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
        installmentAsyncProcessor.createNextInstallment(transaction);
        return successMessage();
    }

    private LoanApplicationMessage successMessage() {
        return new LoanApplicationMessage("Your loan application has been successfully submitted.");
    }

}