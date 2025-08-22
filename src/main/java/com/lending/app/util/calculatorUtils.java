package com.lending.app.util;

import com.lending.app.model.entity.Loan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class calculatorUtils {

    private static double annualRate;

    public calculatorUtils(@Value("${annual.rate}") double annualRate) {
        calculatorUtils.annualRate = annualRate;
    }

    public static long calculateEachInstallmentAmount(long loanAmount, int installmentNumber) {
        return (long) Math.ceil( (double) calculateMustPaidAmount(loanAmount, installmentNumber) / installmentNumber);
    }

    public static long calculateMustPaidAmount(long loanAmount, int installmentNumber) {
        double monthlyRate = annualRate / 12 / 100.0;
        double mustPaidAmount = monthlyRate * installmentNumber * loanAmount + loanAmount;
        return Math.round(mustPaidAmount);
    }

    public static int calculateGuarantorScore(Loan loan) {
        return (int) Math.round(loan.getRequiredScore() * 0.1);
    }

}
