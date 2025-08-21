package com.lending.app.model.entity;

import com.lending.app.model.entity.base.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
public class Loan extends BaseEntity {

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

    private LocalDateTime deletedAt;

}
