package com.spendiq.repository;

import com.spendiq.entity.GroupSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupSplitRepository extends JpaRepository<GroupSplit, Long> {

    @Query("""
        SELECT DISTINCT g FROM GroupSplit g
        JOIN g.members m
        WHERE m.id = :userId
        ORDER BY g.date DESC
    """)
    List<GroupSplit> findAllByMemberId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT g FROM GroupSplit g LEFT JOIN FETCH g.owes WHERE g.id = :id")
    Optional<GroupSplit> findByIdWithOwes(@Param("id") Long id);

    @Query("SELECT g FROM GroupSplit g WHERE g.paidBy.id = :userId ORDER BY g.date DESC")
    List<GroupSplit> findByPaidById(@Param("userId") Long userId);

    // ✅ Uses correct column name: split_id (not group_split_id)
    @Modifying
    @Query(value = "DELETE FROM group_split_members WHERE split_id = :splitId", nativeQuery = true)
    void deleteMembersBySplitId(@Param("splitId") Long splitId);

    @Modifying
    @Query(value = "DELETE FROM group_splits WHERE id = :splitId", nativeQuery = true)
    void deleteByIdNative(@Param("splitId") Long splitId);
}
