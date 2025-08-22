package com.lending.app.application.processor;

import com.lending.app.application.service.InstallmentService;
import com.lending.app.application.service.UserService;
import com.lending.app.model.entity.Installment;
import com.lending.app.model.entity.Loan;
import com.lending.app.model.entity.LoanTransaction;
import com.lending.app.model.entity.User;
import com.lending.app.util.LoanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InstallmentAsyncProcessor {

    private final InstallmentService installmentService;
    private final UserService userService;

    public InstallmentAsyncProcessor(InstallmentService installmentService, UserService userService) {
        this.installmentService = installmentService;
        this.userService = userService;
    }

    @Async("taskExecutor")
    public void createNextInstallment(LoanTransaction transaction) {
        Installment installment = new Installment();
        installment.setLoanTransaction(transaction);
        installment.setDueDate(LocalDateTime.now().plusMonths(1));
        installment.setPaid(false);
        installmentService.save(installment);
    }

    @Async("taskExecutor")
    public void processInstallmentBonus(Installment installment) {
        Loan loan = installment.getLoanTransaction().getLoan();
        int bonusScore = loan.getAwardScore() / loan.getNumberOfInstallments();

        long daysDiff = 0;
        if (installment.getPaymentDate() != null) {
            daysDiff = java.time.temporal.ChronoUnit.DAYS.between(
                    installment.getDueDate(),
                    installment.getPaymentDate()
            );
        }

        double penaltyFactor = 1.0 - (0.01 * daysDiff);
        if (daysDiff < 0) {
            penaltyFactor = 1.0 + (0.01 * Math.abs(daysDiff));
        }
        if (penaltyFactor < 0) {
            penaltyFactor = 0;
        }
        int finalBonus = (int) Math.round(bonusScore * penaltyFactor);

        LoanTransaction loanTransaction = installment.getLoanTransaction();
        User borrower = loanTransaction.getBorrower();

        if (loanTransaction.getGuarantor() != null) {
            User guarantor = loanTransaction.getGuarantor();

            int borrowerBonus = (int) Math.round(finalBonus * 0.9);
            int guarantorBonus = (int) Math.round(finalBonus * 0.1);

            borrower.setScore(borrower.getScore() + borrowerBonus);
            guarantor.setScore(guarantor.getScore() + guarantorBonus);

            userService.save(guarantor);
        } else {
            borrower.setScore(borrower.getScore() + finalBonus);
        }

        userService.save(borrower);
    }

}

