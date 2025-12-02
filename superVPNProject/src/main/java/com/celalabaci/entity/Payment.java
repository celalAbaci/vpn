package com.celalabaci.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.celalabaci.entity.User;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime paymentDate;

    @Column(name = "transaction_id", unique = true, length = 100)
    private String transaction;

    @Column(name = "status", nullable = false, length = 20)
    private String status;
}