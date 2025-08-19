package com.lending.app.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private int numberOfInstallments;

    @Column(nullable = false)
    private int requiredScore;

    @Column(nullable = false)
    private int awardScore;

}
