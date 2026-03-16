package com.spendiq.service;

import com.spendiq.dto.response.AnalyticsResponse;
import com.spendiq.entity.User;
import com.spendiq.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;

    @Autowired
    public AnalyticsService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public AnalyticsResponse getAnalytics(User user, int year) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to   = LocalDate.of(year, 12, 31);

        List<Object[]> rawMonthly = expenseRepository.getMonthlySummary(user.getId(), from, to);
        Map<String, AnalyticsResponse.MonthlySummary> monthMap = new LinkedHashMap<>();

        for (Object[] row : rawMonthly) {
            int month      = ((Number) row[0]).intValue();
            int yr         = ((Number) row[1]).intValue();
            String type    = row[2].toString();
            BigDecimal amt = (BigDecimal) row[3];
            String key     = yr + "-" + month;

            AnalyticsResponse.MonthlySummary entry = monthMap.computeIfAbsent(key, k ->
                AnalyticsResponse.MonthlySummary.builder()
                    .month(month).year(yr)
                    .monthName(Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .income(BigDecimal.ZERO).expense(BigDecimal.ZERO).savings(BigDecimal.ZERO)
                    .build()
            );

            if ("income".equals(type)) entry.setIncome(amt);
            else                       entry.setExpense(amt);
            entry.setSavings(entry.getIncome().subtract(entry.getExpense()));
        }

        List<AnalyticsResponse.MonthlySummary> monthlySummary = new ArrayList<>(monthMap.values());

        BigDecimal totalIncome  = monthlySummary.stream().map(AnalyticsResponse.MonthlySummary::getIncome).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = monthlySummary.stream().map(AnalyticsResponse.MonthlySummary::getExpense).reduce(BigDecimal.ZERO, BigDecimal::add);

        int currentMonth = LocalDate.now().getMonthValue();
        List<Object[]> rawCat = expenseRepository.getCategoryBreakdown(user.getId(), currentMonth, year);
        List<AnalyticsResponse.CategoryBreakdown> catBreakdown = rawCat.stream()
            .map(row -> {
                BigDecimal total = (BigDecimal) row[1];
                double pct = totalExpense.compareTo(BigDecimal.ZERO) > 0
                        ? total.divide(totalExpense, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                        : 0.0;
                return AnalyticsResponse.CategoryBreakdown.builder()
                        .category(row[0].toString())
                        .total(total)
                        .percentage(pct)
                        .build();
            })
            .collect(Collectors.toList());

        long recurringCount = expenseRepository.findByUserIdAndRecurringTrue(user.getId()).size();

        return AnalyticsResponse.builder()
                .monthlySummary(monthlySummary)
                .categoryBreakdown(catBreakdown)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netSavings(totalIncome.subtract(totalExpense))
                .recurringCount(recurringCount)
                .build();
    }
}