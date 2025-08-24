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
import com.lending.app.util.CalculatorUtils;
import com.lending.app.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Slf4j
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
        log.debug("Processing loan application for borrowerId: {} and loanId: {}", borrowerId, application.loanId());

        User borrower = userService.getUserForUpdate(borrowerId);
        Loan loan = loanService.getLoan(application.loanId());

        LoanTransaction transaction = initializeTransaction(loan, borrower);

        LoanApplicationMessage result;
        if (borrower.getScore() >= loan.getRequiredScore()) {
            log.debug("Borrower has enough score: {} >= {}", borrower.getScore(), loan.getRequiredScore());
            result = handleBorrower(borrower, loan, transaction);
        } else {
            log.debug("Borrower does not have enough score: {} < {}", borrower.getScore(), loan.getRequiredScore());
            result = handleWithGuarantor(application, borrower, loan, transaction);
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.debug("Triggering async installment creation for transactionId: {}", transaction.getId());
                installmentAsyncProcessor.handleInstallmentCreation(new LoanTransactionMessage(transaction.getId()));
            }
        });

        log.info("Loan application processed successfully for borrowerId: {} and loanId: {}", borrowerId, loan.getId());
        return result;
    }

    private LoanTransaction initializeTransaction(Loan loan, User borrower) {
        LoanTransaction transaction = new LoanTransaction();
        transaction.setLoan(loan);
        transaction.setBorrower(borrower);
        transaction.setStartDate(LocalDateTime.now());
        log.debug("Initialized LoanTransaction for borrowerId: {} and loanId: {}", borrower.getId(), loan.getId());
        return transaction;
    }

    private LoanApplicationMessage handleBorrower(User borrower, Loan loan, LoanTransaction transaction) {
        loanTransactionService.saveAndFlush(transaction);
        log.debug("Transaction saved for borrowerId: {} and loanId: {}", borrower.getId(), loan.getId());

        userService.changeScore(borrower, -loan.getRequiredScore());
        log.debug("Borrower score reduced by {}. New score: {}", loan.getRequiredScore(), borrower.getScore());

        return successMessage();
    }

    private LoanApplicationMessage handleWithGuarantor(LoanApplicationCommand application, User borrower, Loan loan, LoanTransaction transaction) {
        if (application.guarantorId() == null || application.guarantorId().isBlank()) {
            log.warn("Loan application failed: guarantor not provided for borrowerId: {}", borrower.getId());
            throw new InsufficientScoreException();
        }

        User guarantor = userService.getUserForUpdate(application.guarantorId());
        int neededFromGuarantor = CalculatorUtils.calculateGuarantorScore(loan);
        int neededFromBorrower = loan.getRequiredScore() - neededFromGuarantor;

        if (guarantor.getScore() < neededFromGuarantor) {
            log.warn("Guarantor score insufficient: {} < {} for loanId: {}", guarantor.getScore(), neededFromGuarantor, loan.getId());
            throw new InsufficientScoreException();
        }

        userService.changeScore(borrower, -neededFromBorrower);
        userService.changeScore(guarantor, -neededFromGuarantor);
        log.debug("Scores updated. Borrower new score: {}, Guarantor new score: {}", borrower.getScore(), guarantor.getScore());

        transaction.setGuarantor(guarantor);
        loanTransactionService.saveAndFlush(transaction);
        log.debug("Transaction saved with guarantorId: {}", guarantor.getId());

        return successMessage();
    }

    private LoanApplicationMessage successMessage() {
        return new LoanApplicationMessage("Your loan application has been successfully submitted.");
    }
}