package com.spendiq.dto.response;

import com.spendiq.entity.Expense;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpenseResponse {
    private Long id;
    private String title;
    private BigDecimal amount;
    private LocalDate date;
    private String category;
    private String type;
    private boolean recurring;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ExpenseResponse() {}

    public static ExpenseResponse from(Expense e) {
        ExpenseResponse r = new ExpenseResponse();
        r.id = e.getId(); r.title = e.getTitle(); r.amount = e.getAmount();
        r.date = e.getDate(); r.category = e.getCategory().name();
        r.type = e.getType().name(); r.recurring = e.isRecurring();
        r.createdAt = e.getCreatedAt(); r.updatedAt = e.getUpdatedAt();
        return r;
    }

    public Long getId()             { return id; }
    public String getTitle()        { return title; }
    public BigDecimal getAmount()   { return amount; }
    public LocalDate getDate()      { return date; }
    public String getCategory()     { return category; }
    public String getType()         { return type; }
    public boolean isRecurring()    { return recurring; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}