package com.lending.app.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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
