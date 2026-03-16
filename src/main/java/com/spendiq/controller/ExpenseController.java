package com.spendiq.controller;

import com.spendiq.dto.request.ExpenseRequest;
import com.spendiq.dto.response.ApiResponse;
import com.spendiq.entity.User;
import com.spendiq.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.ok(
                expenseService.getAll(user, type, category, from, to, search)));
    }

    @GetMapping("/recurring")
    public ResponseEntity<?> getRecurring(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(expenseService.getRecurring(user)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(expenseService.getById(user, id)));
    }

    @PostMapping
    public ResponseEntity<?> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Expense created", expenseService.create(user, request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Expense updated", expenseService.update(user, id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        expenseService.delete(user, id);
        return ResponseEntity.ok(ApiResponse.ok("Expense deleted", null));
    }
}