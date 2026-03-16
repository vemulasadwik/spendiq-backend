package com.spendiq.repository;

import com.spendiq.entity.SplitOwe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SplitOweRepository extends JpaRepository<SplitOwe, Long> {

    List<SplitOwe> findByGroupSplitId(Long splitId);

    Optional<SplitOwe> findByGroupSplitIdAndUserId(Long splitId, Long userId);

    List<SplitOwe> findByUserIdAndPaidFalse(Long userId);
}
