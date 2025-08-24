package com.lending.app.model.record.loan;

import java.util.Set;

public record LoanMessageSet(
        Set<LoanMessage> loanMessages
) {
}
