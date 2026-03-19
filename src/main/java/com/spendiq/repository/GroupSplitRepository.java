package com.spendiq.repository;

import com.spendiq.entity.GroupSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupSplitRepository extends JpaRepository<GroupSplit, Long> {

    // All splits where user is a member (either payer or ower)
    @Query("""
        SELECT DISTINCT g FROM GroupSplit g
        JOIN g.members m
        WHERE m.id = :userId
        ORDER BY g.date DESC
    """)
    List<GroupSplit> findAllByMemberId(@Param("userId") Long userId);

    // Fetch split with owes eagerly loaded
    @Query("SELECT DISTINCT g FROM GroupSplit g LEFT JOIN FETCH g.owes WHERE g.id = :id")
    Optional<GroupSplit> findByIdWithOwes(@Param("id") Long id);

    // Splits where this user is the payer
    @Query("SELECT g FROM GroupSplit g WHERE g.paidBy.id = :userId ORDER BY g.date DESC")
    List<GroupSplit> findByPaidById(@Param("userId") Long userId);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM group_split_members WHERE group_split_id = :splitId", nativeQuery = true)
    void deleteMembersByGroupSplitId(@org.springframework.data.repository.query.Param("splitId") Long splitId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM group_splits WHERE id = :splitId", nativeQuery = true)
    void deleteByIdNative(@org.springframework.data.repository.query.Param("splitId") Long splitId);
}
