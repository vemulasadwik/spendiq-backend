package com.spendiq.repository;

import com.spendiq.entity.Expense;
import com.spendiq.entity.Expense.Category;
import com.spendiq.entity.Expense.EntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // All entries for a user, newest first
    List<Expense> findByUserIdOrderByDateDesc(Long userId);

    // Filter by type (expense / income)
    List<Expense> findByUserIdAndTypeOrderByDateDesc(Long userId, EntryType type);

    // Filter by category
    List<Expense> findByUserIdAndCategoryOrderByDateDesc(Long userId, Category category);

    // Filter by date range
    List<Expense> findByUserIdAndDateBetweenOrderByDateDesc(Long userId, LocalDate from, LocalDate to);

    // Recurring entries only
    List<Expense> findByUserIdAndRecurringTrue(Long userId);

    // Monthly summary for analytics (income vs expense per month)
    @Query("""
        SELECT MONTH(e.date) as month, YEAR(e.date) as year,
               e.type as type, SUM(e.amount) as total
        FROM Expense e
        WHERE e.user.id = :userId
          AND e.date BETWEEN :from AND :to
        GROUP BY YEAR(e.date), MONTH(e.date), e.type
        ORDER BY YEAR(e.date), MONTH(e.date)
    """)
    List<Object[]> getMonthlySummary(@Param("userId") Long userId,
                                     @Param("from") LocalDate from,
                                     @Param("to") LocalDate to);

    // Category breakdown for current month
    @Query("""
        SELECT e.category as category, SUM(e.amount) as total
        FROM Expense e
        WHERE e.user.id = :userId
          AND e.type = 'expense'
          AND MONTH(e.date) = :month
          AND YEAR(e.date) = :year
        GROUP BY e.category
    """)
    List<Object[]> getCategoryBreakdown(@Param("userId") Long userId,
                                         @Param("month") int month,
                                         @Param("year") int year);

    // Full search: title contains keyword
    @Query("""
        SELECT e FROM Expense e
        WHERE e.user.id = :userId
          AND LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY e.date DESC
    """)
    List<Expense> searchByTitle(@Param("userId") Long userId, @Param("keyword") String keyword);
}
