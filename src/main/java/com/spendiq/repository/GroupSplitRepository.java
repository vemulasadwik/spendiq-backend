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

    @Query("SELECT DISTINCT g FROM GroupSplit g JOIN g.members m WHERE m.id = :userId")
    List<GroupSplit> findAllByMemberId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT g FROM GroupSplit g LEFT JOIN FETCH g.owes o LEFT JOIN FETCH o.user LEFT JOIN FETCH g.paidBy WHERE g.id = :id")
    Optional<GroupSplit> findByIdWithOwes(@Param("id") Long id);
}