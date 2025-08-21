package com.lending.app.entity;

import com.lending.app.entity.base.BaseEntity;
import com.lending.app.enums.ScoreTransactionType;
import jakarta.persistence.*;

@Entity
@Table
public class ScoreTransaction extends BaseEntity {

    @ManyToOne
    private User fromUser;

    @ManyToOne(optional = false)
    private User toUser;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    private ScoreTransactionType type;

}
