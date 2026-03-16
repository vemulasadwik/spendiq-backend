package com.spendiq.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class AnalyticsResponse {
    private List<MonthlySummary> monthlySummary;
    private List<CategoryBreakdown> categoryBreakdown;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netSavings;
    private long recurringCount;

    private AnalyticsResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private List<MonthlySummary> monthlySummary;
        private List<CategoryBreakdown> categoryBreakdown;
        private BigDecimal totalIncome, totalExpense, netSavings;
        private long recurringCount;
        public Builder monthlySummary(List<MonthlySummary> v)      { this.monthlySummary = v;    return this; }
        public Builder categoryBreakdown(List<CategoryBreakdown> v) { this.categoryBreakdown = v; return this; }
        public Builder totalIncome(BigDecimal v)   { this.totalIncome = v;   return this; }
        public Builder totalExpense(BigDecimal v)  { this.totalExpense = v;  return this; }
        public Builder netSavings(BigDecimal v)    { this.netSavings = v;    return this; }
        public Builder recurringCount(long v)      { this.recurringCount = v; return this; }
        public AnalyticsResponse build() {
            AnalyticsResponse r = new AnalyticsResponse();
            r.monthlySummary = monthlySummary; r.categoryBreakdown = categoryBreakdown;
            r.totalIncome = totalIncome; r.totalExpense = totalExpense;
            r.netSavings = netSavings; r.recurringCount = recurringCount;
            return r;
        }
    }

    // ── MonthlySummary ──────────────────────────────────────────────────────
    public static class MonthlySummary {
        private int month, year;
        private String monthName;
        private BigDecimal income, expense, savings;

        private MonthlySummary() {}

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private int month, year; private String monthName;
            private BigDecimal income, expense, savings;
            public Builder month(int v)         { this.month = v;     return this; }
            public Builder year(int v)          { this.year = v;      return this; }
            public Builder monthName(String v)  { this.monthName = v; return this; }
            public Builder income(BigDecimal v)  { this.income = v;    return this; }
            public Builder expense(BigDecimal v) { this.expense = v;   return this; }
            public Builder savings(BigDecimal v) { this.savings = v;   return this; }
            public MonthlySummary build() {
                MonthlySummary r = new MonthlySummary();
                r.month = month; r.year = year; r.monthName = monthName;
                r.income = income; r.expense = expense; r.savings = savings;
                return r;
            }
        }

        public int getMonth()           { return month; }
        public int getYear()            { return year; }
        public String getMonthName()    { return monthName; }
        public BigDecimal getIncome()   { return income; }
        public BigDecimal getExpense()  { return expense; }
        public BigDecimal getSavings()  { return savings; }
        public void setIncome(BigDecimal v)  { this.income = v; }
        public void setExpense(BigDecimal v) { this.expense = v; }
        public void setSavings(BigDecimal v) { this.savings = v; }
    }

    // ── CategoryBreakdown ───────────────────────────────────────────────────
    public static class CategoryBreakdown {
        private String category;
        private BigDecimal total;
        private double percentage;

        private CategoryBreakdown() {}

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String category; private BigDecimal total; private double percentage;
            public Builder category(String v)   { this.category = v;   return this; }
            public Builder total(BigDecimal v)   { this.total = v;      return this; }
            public Builder percentage(double v)  { this.percentage = v; return this; }
            public CategoryBreakdown build() {
                CategoryBreakdown r = new CategoryBreakdown();
                r.category = category; r.total = total; r.percentage = percentage;
                return r;
            }
        }

        public String getCategory()    { return category; }
        public BigDecimal getTotal()   { return total; }
        public double getPercentage()  { return percentage; }
    }

    public List<MonthlySummary> getMonthlySummary()        { return monthlySummary; }
    public List<CategoryBreakdown> getCategoryBreakdown()  { return categoryBreakdown; }
    public BigDecimal getTotalIncome()  { return totalIncome; }
    public BigDecimal getTotalExpense() { return totalExpense; }
    public BigDecimal getNetSavings()   { return netSavings; }
    public long getRecurringCount()     { return recurringCount; }
}