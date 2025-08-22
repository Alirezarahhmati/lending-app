package com.lending.app.model.entity;

import com.lending.app.model.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table
public class Installment extends BaseEntity {

    @ManyToOne
    @JoinColumn(nullable = false)
    private LoanTransaction loanTransaction;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    private LocalDateTime paymentDate;

    @Column(nullable = false)
    private boolean paid;

}
