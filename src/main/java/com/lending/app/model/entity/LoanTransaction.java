package com.lending.app.model.entity;

import com.lending.app.model.entity.base.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table
public class LoanTransaction extends BaseEntity {

    @ManyToOne(optional = false)
    private User borrower;

    @ManyToOne(optional = false)
    private Loan loan;

    @Column(nullable = false)
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Column(nullable = false)
    private long paidAmount;

    @ManyToOne
    private User guarantor;

}
