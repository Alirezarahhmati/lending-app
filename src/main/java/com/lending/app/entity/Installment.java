package com.lending.app.entity;

import com.lending.app.entity.base.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table
public class Installment extends BaseEntity {

    @ManyToOne
    private LoanTransaction loanTransaction;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    private LocalDateTime paymentDate;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private long paidAmount;

    @Column(nullable = false)
    private boolean paid;

}
