package com.spendiq.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType type;

    @Column(nullable = false)
    private boolean recurring;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Expense() {}

    private Expense(Builder b) {
        this.title = b.title; this.amount = b.amount; this.date = b.date;
        this.category = b.category; this.type = b.type;
        this.recurring = b.recurring; this.user = b.user;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String title; private BigDecimal amount; private LocalDate date;
        private Category category; private EntryType type;
        private boolean recurring; private User user;
        public Builder title(String v)      { this.title = v;     return this; }
        public Builder amount(BigDecimal v)  { this.amount = v;    return this; }
        public Builder date(LocalDate v)     { this.date = v;      return this; }
        public Builder category(Category v)  { this.category = v;  return this; }
        public Builder type(EntryType v)     { this.type = v;      return this; }
        public Builder recurring(boolean v)  { this.recurring = v; return this; }
        public Builder user(User v)          { this.user = v;      return this; }
        public Expense build() { return new Expense(this); }
    }

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId()             { return id; }
    public String getTitle()        { return title; }
    public BigDecimal getAmount()   { return amount; }
    public LocalDate getDate()      { return date; }
    public Category getCategory()   { return category; }
    public EntryType getType()      { return type; }
    public boolean isRecurring()    { return recurring; }
    public User getUser()           { return user; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setTitle(String v)      { this.title = v; }
    public void setAmount(BigDecimal v)  { this.amount = v; }
    public void setDate(LocalDate v)     { this.date = v; }
    public void setCategory(Category v)  { this.category = v; }
    public void setType(EntryType v)     { this.type = v; }
    public void setRecurring(boolean v)  { this.recurring = v; }

    public enum Category { Food, Entertainment, Utilities, Health, Transport, Shopping, Income, Other }
    public enum EntryType { expense, income }
}