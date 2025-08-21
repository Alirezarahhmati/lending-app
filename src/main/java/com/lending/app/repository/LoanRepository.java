package com.lending.app.repository;

import com.lending.app.model.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, String> {

    @Override
    @Query(value = "SELECT * FROM loan WHERE id = :id AND deleted_at IS NULL LIMIT 1", nativeQuery = true)
    Optional<Loan> findById(@Param("id") String id);

    @Override
    @Query(value = "SELECT CASE WHEN COUNT(1) > 0 THEN TRUE ELSE FALSE END FROM loan WHERE id = :id AND deleted_at IS NULL", nativeQuery = true)
    boolean existsById(@Param("id") String id);

    @Override
    @Query(value = "SELECT * FROM loan WHERE deleted_at IS NULL", nativeQuery = true)
    List<Loan> findAll();

    @Query(value = "UPDATE loan SET deleted_at = CURRENT_TIMESTAMP WHERE id = :id", nativeQuery = true)
    @org.springframework.data.jpa.repository.Modifying
    void softDeleteById(@Param("id") String id);
}



