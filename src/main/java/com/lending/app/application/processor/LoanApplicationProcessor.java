package com.lending.app.application.processor;

import com.lending.app.application.service.LoanService;
import com.lending.app.application.service.UserService;
import com.lending.app.model.entity.Loan;
import com.lending.app.model.entity.LoanTransaction;
import com.lending.app.model.entity.User;
import com.lending.app.model.record.loan.LoanApplicationCommand;
import com.lending.app.model.record.loan.LoanApplicationMessage;
import com.lending.app.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class LoanApplicationProcessor {

    private final UserService userService;
    private final LoanService loanService;

    public LoanApplicationProcessor(UserService userService, LoanService loanService) {
        this.userService = userService;
        this.loanService = loanService;
    }

    @Transactional
    public LoanApplicationMessage process(LoanApplicationCommand application){
        String borrowerId = SecurityUtils.getCurrentUserId();
        User borrower = userService.getUser(borrowerId);
        Loan loan = loanService.getLoan(application.loanId());

        int totalScore = borrower.getScore();

        if (totalScore >= loan.getRequiredScore()) {
            LoanTransaction loanTransaction = new LoanTransaction();
            loanTransaction.setBorrower(borrower);
            loanTransaction.setLoan(loan);
            loanTransaction.setStartDate(LocalDateTime.now());
            loanTransaction.setPaidAmount(0);
            // todo: save the loanTransaction

            userService.decreaseScore(borrowerId, loan.getRequiredScore());
            return new LoanApplicationMessage("Loan application processed successfully");
        }

        if (ObjectUtils.isEmpty(application.guarantorId())) {
            return new LoanApplicationMessage("Your score does not enough for loan application, you can try with guarantor or try again later.");
        }

        User guarantor = userService.getUser(application.guarantorId());
        int neededScore = (int) Math.round(loan.getRequiredScore() * 0.1);
        if (guarantor.getScore() < neededScore || loan.getRequiredScore() < ) {
            return new LoanApplicationMessage("Your score does not enough for loan application, you can try again later.");
        }



    }

    protected int calculateTotalScore(User borrower, User guarantor) {
        int guarantorScore = guarantor != null ? guarantor.getScore() : 0;
        return borrower.getScore() + guarantorScore * 1.1;
    }
}
