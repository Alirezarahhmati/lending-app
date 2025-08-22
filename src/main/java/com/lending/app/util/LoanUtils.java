package com.lending.app.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoanUtils {

    private static double annualRate;

    public LoanUtils(@Value("${annual.rate}") double annualRate) {
        LoanUtils.annualRate = annualRate;
    }

    public static long calculateEachInstallmentAmount(long loanAmount, int installmentNumber) {
        return (long) Math.ceil( (double) calculateMustPaidAmount(loanAmount, installmentNumber) / installmentNumber);
    }

    public static long calculateMustPaidAmount(long loanAmount, int installmentNumber) {
        double monthlyRate = annualRate / 12 / 100.0;
        double mustPaidAmount = monthlyRate * installmentNumber * loanAmount + loanAmount;
        return Math.round(mustPaidAmount);
    }

}
