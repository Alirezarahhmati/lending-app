package com.lending.app.controller;

import com.lending.app.model.record.base.BaseResponse;
import com.lending.app.model.record.loan.LoanMessage;
import com.lending.app.model.record.loan.SaveLoanCommand;
import com.lending.app.model.record.loan.UpdateLoanCommand;
import com.lending.app.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    public ResponseEntity<BaseResponse<LoanMessage>> save(@Valid @RequestBody SaveLoanCommand command) {
        return BaseResponse.success(loanService.save(command));
    }

    @PutMapping
    public ResponseEntity<BaseResponse<LoanMessage>> update(@Valid @RequestBody UpdateLoanCommand command) {
        return BaseResponse.success(loanService.update(command));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable String id) {
        loanService.delete(id);
        return BaseResponse.success(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<LoanMessage>> get(@PathVariable String id) {
        return BaseResponse.success(loanService.get(id));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<LoanMessage>>> getAll() {
        return BaseResponse.success(loanService.getAll());
    }
}


