package com.lending.app.model.record.loan;

import com.lending.app.model.entity.Loan;
import java.time.LocalDateTime;

public record UserLoanTransactionMessage(
        String loanTransactionId,
        Loan loan,
        long paidAmount,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
