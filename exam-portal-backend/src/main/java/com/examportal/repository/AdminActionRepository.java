package com.examportal.repository;

import com.examportal.model.AdminAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminActionRepository extends JpaRepository<AdminAction, Long> {

    List<AdminAction> findByAdminIdOrderByPerformedAtDesc(Long adminId);

    List<AdminAction> findByTargetUserIdOrderByPerformedAtDesc(Long targetUserId);

    List<AdminAction> findByActionTypeOrderByPerformedAtDesc(AdminAction.ActionType actionType);

    List<AdminAction> findByTop20ByOrderByPerformedAtDesc();

    @Query("SELECT a FROM AdminAction a "+ "WHERE a.performedAt BETWEEN :from AND :to "+ "ORDER BY a.performedAt DESC")
    List<AdminAction> findByDateRange(LocalDateTime from , LocalDateTime to);

    @Query("SELECT a.actionType, COUNT(a) FROM AdminAction a "+ "GROUP BY a.actionType")
    List<Object[]> countByActionType();
}
