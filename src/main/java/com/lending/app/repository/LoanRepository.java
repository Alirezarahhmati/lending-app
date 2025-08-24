package com.lending.app.repository;

import com.lending.app.model.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, String> {

    @Query(
            value = """
        SELECT EXISTS (
            SELECT 1
            FROM loan
            WHERE name = :name
              AND deleted_at IS NULL
        )
    """,
            nativeQuery = true
    )
    boolean existsByNameAndDeletedAtIsNull(@Param("name") String name);


    @Override
    @Query(
            value = """
            SELECT * FROM loan
            WHERE id = :id
              AND deleted_at IS NULL
        """,
            nativeQuery = true
    )
    Optional<Loan> findById(@Param("id") String id);

    @Override
    @Query(
            value = """
            SELECT EXISTS (
                SELECT 1 
                FROM loan
                WHERE id = :id
                  AND deleted_at IS NULL
            )
        """,
            nativeQuery = true
    )
    boolean existsById(@Param("id") String id);

    @Override
    @Query(
            value = """
            SELECT * FROM loan
            WHERE deleted_at IS NULL
        """,
            nativeQuery = true
    )
    List<Loan> findAll();

    @Modifying
    @Query(
            value = """
            UPDATE loan
            SET deleted_at = CURRENT_TIMESTAMP
            WHERE id = :id
        """,
            nativeQuery = true
    )
    void softDeleteById(@Param("id") String id);
}