package com.lending.app.model.entity;

import com.lending.app.model.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_loan_name_deleted", columnNames = {"name", "deleted_at"})
        },
        indexes = {
                @Index(name = "idx_loan_name_deleted", columnList = "name, deleted_at"),
                @Index(name = "idx_loan_deleted_at", columnList = "deleted_at")
        }
)
public class Loan extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private int numberOfInstallments;

    @Column(nullable = false)
    private long eachInstallmentAmount;

    @Column(nullable = false)
    private int requiredScore;

    @Column(nullable = false)
    private int awardScore;

    private LocalDateTime deletedAt;

}
