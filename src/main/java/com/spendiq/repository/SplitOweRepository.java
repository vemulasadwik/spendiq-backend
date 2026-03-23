package com.spendiq.repository;

import com.spendiq.entity.SplitOwe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SplitOweRepository extends JpaRepository<SplitOwe, Long> {

    List<SplitOwe> findByGroupSplitId(Long splitId);

    Optional<SplitOwe> findByGroupSplitIdAndUserId(Long splitId, Long userId);

    List<SplitOwe> findByUserIdAndPaidFalse(Long userId);

    // ✅ Uses correct column name: split_id (not group_split_id)
    @Modifying
    @Query(value = "DELETE FROM split_owes WHERE split_id = :splitId", nativeQuery = true)
    void deleteOwesBySplitId(@Param("splitId") Long splitId);
}
