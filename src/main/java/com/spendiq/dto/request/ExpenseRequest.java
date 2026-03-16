package com.spendiq.dto.request;

import com.spendiq.entity.Expense.Category;
import com.spendiq.entity.Expense.EntryType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Category is required")
    private Category category;

    @NotNull(message = "Type is required")
    private EntryType type;

    private boolean recurring;

    public String getTitle()      { return title; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDate()    { return date; }
    public Category getCategory() { return category; }
    public EntryType getType()    { return type; }
    public boolean isRecurring()  { return recurring; }

    public void setTitle(String v)      { this.title = v; }
    public void setAmount(BigDecimal v)  { this.amount = v; }
    public void setDate(LocalDate v)     { this.date = v; }
    public void setCategory(Category v)  { this.category = v; }
    public void setType(EntryType v)     { this.type = v; }
    public void setRecurring(boolean v)  { this.recurring = v; }
}