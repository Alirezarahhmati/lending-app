package com.lending.app.repository;

import com.lending.app.model.entity.Loan;
import com.lending.app.model.entity.LoanTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanTransactionRepository extends JpaRepository<LoanTransaction, String> {

    @Override
    @Query(
            value = "SELECT * FROM loan_transaction " +
                    "WHERE id = :id ",
            nativeQuery = true
    )
    Optional<LoanTransaction> findById(@Param("id") String id);

}
