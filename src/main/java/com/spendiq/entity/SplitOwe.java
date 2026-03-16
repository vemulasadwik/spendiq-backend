package com.spendiq.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "split_owes")
public class SplitOwe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "split_id", nullable = false)
    private GroupSplit groupSplit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private boolean paid = false;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public SplitOwe() {}

    private SplitOwe(Builder b) {
        this.groupSplit = b.groupSplit; this.user = b.user; this.amount = b.amount;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private GroupSplit groupSplit; private User user; private BigDecimal amount;
        public Builder groupSplit(GroupSplit v) { this.groupSplit = v; return this; }
        public Builder user(User v)             { this.user = v;       return this; }
        public Builder amount(BigDecimal v)     { this.amount = v;     return this; }
        public SplitOwe build() { return new SplitOwe(this); }
    }

    public void markPaid()   { this.paid = true;  this.paidAt = LocalDateTime.now(); }
    public void unmarkPaid() { this.paid = false; this.paidAt = null; }

    public Long getId()             { return id; }
    public GroupSplit getGroupSplit(){ return groupSplit; }
    public User getUser()           { return user; }
    public BigDecimal getAmount()   { return amount; }
    public boolean isPaid()         { return paid; }
    public LocalDateTime getPaidAt(){ return paidAt; }
}