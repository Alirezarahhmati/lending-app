package com.lending.app.application.processor;

import com.lending.app.application.service.InstallmentService;
import com.lending.app.application.service.LoanTransactionService;
import com.lending.app.application.service.UserService;
import com.lending.app.model.entity.Installment;
import com.lending.app.model.entity.Loan;
import com.lending.app.model.entity.LoanTransaction;
import com.lending.app.model.entity.User;
import com.lending.app.model.record.loan.LoanTransactionMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InstallmentAsyncProcessor {

    private final InstallmentService installmentService;
    private final LoanTransactionService loanTransactionService;
    private final UserService userService;


    public InstallmentAsyncProcessor(
            InstallmentService installmentService,
            LoanTransactionService loanTransactionService,
            UserService userService
    ) {
        this.installmentService = installmentService;
        this.loanTransactionService = loanTransactionService;
        this.userService = userService;
    }

    @Transactional
    @Async("taskExecutor")
    public void handleInstallmentCreation(LoanTransactionMessage message) {
        LoanTransaction transaction = loanTransactionService.findById(message.transactionId());
        Installment installment = new Installment();
        installment.setLoanTransaction(transaction);
        installment.setDueDate(LocalDateTime.now().plusMonths(1));
        installmentService.save(installment);
    }

    @Transactional
    @Async("taskExecutor")
    public void processInstallmentBonus(Installment installment) {
        int finalBonus = calculateBonus(installment);

        LoanTransaction loanTransaction = installment.getLoanTransaction();
        User borrower = loanTransaction.getBorrower();

        if (loanTransaction.getGuarantor() != null) {
            User guarantor = loanTransaction.getGuarantor();
            int borrowerBonus = (int) Math.round(finalBonus * 0.9);
            int guarantorBonus = finalBonus - borrowerBonus;

            userService.changeScore(borrower, borrowerBonus);
            userService.changeScore(guarantor, guarantorBonus);
        } else {
            userService.changeScore(borrower, finalBonus);
        }
    }

    private int calculateBonus(Installment installment) {
        Loan loan = installment.getLoanTransaction().getLoan();
        int baseBonus = loan.getAwardScore() / loan.getNumberOfInstallments();

        long daysDiff = 0;
        if (installment.getPaymentDate() != null) {
            daysDiff = java.time.temporal.ChronoUnit.DAYS.between(
                    installment.getDueDate(),
                    installment.getPaymentDate()
            );
        }

        double factor = 1.0 - (0.01 * daysDiff);
        if (daysDiff < 0) {
            factor = 1.0 + (0.01 * Math.abs(daysDiff));
        }
        if (factor < 0) {
            factor = 0;
        }

        return (int) Math.round(baseBonus * factor);
    }

}

