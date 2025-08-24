package com.lending.app.controller;

import com.lending.app.application.service.LoanTransactionService;
import com.lending.app.model.record.base.BaseResponse;
import com.lending.app.model.record.loan.UserLoanTransactionMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/loan-transactions")
public class LoanTransactionController {

    private final LoanTransactionService loanTransactionService;

    public LoanTransactionController(LoanTransactionService loanTransactionService) {
        this.loanTransactionService = loanTransactionService;
    }

    @GetMapping("/my-loans")
    public ResponseEntity<BaseResponse<List<UserLoanTransactionMessage>>> getUserLoans() {
        return BaseResponse.success(loanTransactionService.findUserLoanTransactionsByUserId());
    }

}
