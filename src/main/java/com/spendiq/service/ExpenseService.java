package com.spendiq.service;

import com.spendiq.dto.request.ExpenseRequest;
import com.spendiq.dto.response.ExpenseResponse;
import com.spendiq.entity.Expense;
import com.spendiq.entity.Expense.Category;
import com.spendiq.entity.Expense.EntryType;
import com.spendiq.entity.User;
import com.spendiq.exception.BadRequestException;
import com.spendiq.exception.ResourceNotFoundException;
import com.spendiq.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<ExpenseResponse> getAll(User user, String type, String category,
                                        String from, String to, String search) {
        List<Expense> expenses;

        if (search != null && !search.isBlank()) {
            expenses = expenseRepository.searchByTitle(user.getId(), search.trim());
        } else if (from != null && to != null) {
            expenses = expenseRepository.findByUserIdAndDateBetweenOrderByDateDesc(
                    user.getId(), LocalDate.parse(from), LocalDate.parse(to));
        } else if (type != null) {
            expenses = expenseRepository.findByUserIdAndTypeOrderByDateDesc(
                    user.getId(), EntryType.valueOf(type));
        } else if (category != null) {
            expenses = expenseRepository.findByUserIdAndCategoryOrderByDateDesc(
                    user.getId(), Category.valueOf(category));
        } else {
            expenses = expenseRepository.findByUserIdOrderByDateDesc(user.getId());
        }

        return expenses.stream().map(ExpenseResponse::from).collect(Collectors.toList());
    }

    public ExpenseResponse getById(User user, Long id) {
        return ExpenseResponse.from(findAndAuthorize(user, id));
    }

    @Transactional
    public ExpenseResponse create(User user, ExpenseRequest request) {
        Expense expense = Expense.builder()
                .title(request.getTitle())
                .amount(request.getAmount())
                .date(request.getDate())
                .category(request.getCategory())
                .type(request.getType())
                .recurring(request.isRecurring())
                .user(user)
                .build();
        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse update(User user, Long id, ExpenseRequest request) {
        Expense expense = findAndAuthorize(user, id);
        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setDate(request.getDate());
        expense.setCategory(request.getCategory());
        expense.setType(request.getType());
        expense.setRecurring(request.isRecurring());
        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional
    public void delete(User user, Long id) {
        expenseRepository.delete(findAndAuthorize(user, id));
    }

    public List<ExpenseResponse> getRecurring(User user) {
        return expenseRepository.findByUserIdAndRecurringTrue(user.getId())
                .stream().map(ExpenseResponse::from).collect(Collectors.toList());
    }

    private Expense findAndAuthorize(User user, Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied");
        }
        return expense;
    }
}