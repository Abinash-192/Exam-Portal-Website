package com.examportal.repository;

import com.examportal.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    // ── All notifications for user newest first ───────────────────
    List<Notification> findByUserIdOrderByCreatedAtDesc(
            Long userId);

    // ── Unread notifications ──────────────────────────────────────
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(
            Long userId);

    // ── Unread count ──────────────────────────────────────────────
    long countByUserIdAndReadFalse(Long userId);

    // ── Mark all read for user ────────────────────────────────────
    @Modifying
    @Query("UPDATE Notification n SET n.read = true " +
            "WHERE n.user.id = :userId")
    void markAllReadForUser(Long userId);

    // ── Mark single notification read ─────────────────────────────
    @Modifying
    @Query("UPDATE Notification n SET n.read = true " +
            "WHERE n.id = :id")
    void markReadById(Long id);
}