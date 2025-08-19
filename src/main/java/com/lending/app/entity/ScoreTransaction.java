package com.lending.app.entity;

import com.lending.app.enums.ScoreTransactionType;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table
public class ScoreTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private User fromUser;

    @ManyToOne(optional = false)
    private User toUser;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    private ScoreTransactionType type;

}
