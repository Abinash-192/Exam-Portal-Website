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

    // All notifications for a user newest first
    List<Notification> findByUserIdOrderByCreatedAtDesc(
            Long userId);

    // Unread notifications for a user
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(
            Long userId);

    // Count unread — used in UserService.getDashboard()
    long countByUserIdAndReadFalse(Long userId);

    // Mark all read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.read = true " +
            "WHERE n.user.id = :userId")
    void markAllReadForUser(Long userId);
}