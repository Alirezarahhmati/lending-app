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
import com.lending.app.util.LoanUtils;
import com.lending.app.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoanApplicationProcessor {

    private final LoanTransactionService loanTransactionService;
    private final InstallmentAsyncProcessor installmentAsyncProcessor;
    private final UserService userService;
    private final LoanService loanService;

    public LoanApplicationProcessor(
            LoanTransactionService loanTransactionService,
            InstallmentAsyncProcessor installmentAsyncProcessor,
            UserService userService,
            LoanService loanService
    ) {
        this.loanTransactionService = loanTransactionService;
        this.installmentAsyncProcessor = installmentAsyncProcessor;
        this.userService = userService;
        this.loanService = loanService;
    }

    @Transactional
    public LoanApplicationMessage process(LoanApplicationCommand application) {
        String borrowerId = SecurityUtils.getCurrentUserId();
        User borrower = userService.getUserForUpdate(borrowerId);
        Loan loan = loanService.getLoan(application.loanId());

        // 1. Create empty transaction
        LoanTransaction transaction = initializeTransaction(loan, borrower);

        // 2. Try borrower-only eligibility
        if (isBorrowerEligible(borrower, loan)) {
            approveBorrowerOnly(transaction, borrowerId, loan);
            installmentAsyncProcessor.createNextInstallment(transaction);
            return successMessage();
        }

        // 3. If no guarantor provided → fail
        if (application.guarantorId() == null || application.guarantorId().isBlank()) {
            throw new InsufficientScoreException();
        }

        // 4. Try with guarantor
        User guarantor = userService.getUserForUpdate(application.guarantorId());
        if (!isGuarantorEligible(guarantor, loan)) {
            throw new InsufficientScoreException();
        }

        approveWithGuarantor(transaction, borrowerId, guarantor, loan);
        installmentAsyncProcessor.createNextInstallment(transaction);
        return successMessage();
    }

    private LoanTransaction initializeTransaction(Loan loan, User borrower) {
        LoanTransaction transaction = new LoanTransaction();
        transaction.setLoan(loan);
        transaction.setBorrower(borrower);
        transaction.setPaidAmount(0);
        return transaction;
    }

    private boolean isBorrowerEligible(User borrower, Loan loan) {
        return borrower.getScore() >= loan.getRequiredScore();
    }

    private void approveBorrowerOnly(LoanTransaction transaction, String borrowerId, Loan loan) {
        transaction.setStartDate(LocalDateTime.now());
        loanTransactionService.save(transaction);
        userService.decreaseScore(borrowerId, loan.getRequiredScore());
    }

    private boolean isGuarantorEligible(User guarantor, Loan loan) {
        int neededScore = calculateGuarantorScore(loan);
        return guarantor.getScore() >= neededScore;
    }

    private void approveWithGuarantor(LoanTransaction transaction, String borrowerId, User guarantor, Loan loan) {
        int neededFromGuarantor = calculateGuarantorScore(loan);
        int neededFromBorrower = loan.getRequiredScore() - neededFromGuarantor;

        userService.decreaseScore(borrowerId, neededFromBorrower);
        userService.decreaseScore(guarantor.getId(), neededFromGuarantor);

        transaction.setStartDate(LocalDateTime.now());
        transaction.setGuarantor(guarantor);
        loanTransactionService.save(transaction);
    }

    private int calculateGuarantorScore(Loan loan) {
        return (int) Math.round(loan.getRequiredScore() * 0.1);
    }

    private LoanApplicationMessage successMessage() {
        return new LoanApplicationMessage("Your loan application has been successfully submitted.");
    }

}
