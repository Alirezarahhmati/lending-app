package com.lending.app.controller;

import com.lending.app.application.processor.InstallmentPaymentProcessor;
import com.lending.app.application.processor.LoanApplicationProcessor;
import com.lending.app.model.record.base.BaseResponse;
import com.lending.app.model.record.loan.LoanApplicationCommand;
import com.lending.app.model.record.loan.LoanApplicationMessage;
import com.lending.app.model.record.loan.LoanInstallmentCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operation")
public class LoanOperationController {

    private final LoanApplicationProcessor loanApplicationProcessor;
    private final InstallmentPaymentProcessor installmentPaymentProcessor;

    public LoanOperationController(LoanApplicationProcessor loanApplicationProcessor, InstallmentPaymentProcessor installmentPaymentProcessor) {
        this.loanApplicationProcessor = loanApplicationProcessor;
        this.installmentPaymentProcessor = installmentPaymentProcessor;
    }

    @PostMapping("/loan")
    public ResponseEntity<BaseResponse<LoanApplicationMessage>> processLoanTransaction(@RequestBody LoanApplicationCommand application) {
        return BaseResponse.success(loanApplicationProcessor.process(application));
    }

    @PostMapping("/installment")
    public ResponseEntity<BaseResponse<LoanApplicationMessage>> processLoanInstallment(@RequestBody LoanInstallmentCommand installment) {
        return BaseResponse.success(installmentPaymentProcessor.process(installment));
    }
}
