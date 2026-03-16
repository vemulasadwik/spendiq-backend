package com.spendiq.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GroupSplitRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal totalAmount;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private String note;

    @NotNull(message = "Paid-by user ID is required")
    private Long paidByUserId;

    @NotNull(message = "Members are required")
    @Size(min = 2, message = "At least 2 members are required")
    private List<Long> memberUserIds;

    public String getTitle()              { return title; }
    public BigDecimal getTotalAmount()    { return totalAmount; }
    public LocalDate getDate()            { return date; }
    public String getNote()               { return note; }
    public Long getPaidByUserId()         { return paidByUserId; }
    public List<Long> getMemberUserIds()  { return memberUserIds; }

    public void setTitle(String v)           { this.title = v; }
    public void setTotalAmount(BigDecimal v)  { this.totalAmount = v; }
    public void setDate(LocalDate v)          { this.date = v; }
    public void setNote(String v)             { this.note = v; }
    public void setPaidByUserId(Long v)       { this.paidByUserId = v; }
    public void setMemberUserIds(List<Long> v){ this.memberUserIds = v; }
}