package com.lending.app.repository;

import com.lending.app.model.entity.Installment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstallmentRepository extends JpaRepository<Installment, String> {

    @NativeQuery(
            value = "SELECT COUNT(*) FROM installment i " +
                    "WHERE i.loan_transaction_id = :loanTransactionId " +
                    "AND i.paid = true"
    )
    int countPaidByLoanTransactionId(@Param("loanTransactionId") String loanTransactionId);

    @NativeQuery(
            value = "SELECT * FROM installment i " +
                    "WHERE i.loan_transaction_id = :loanTransactionId " +
                    "AND i.paid = false"
    )
    Optional<Installment> findLastByLoanTransactionId(String loanTransactionId);
}
