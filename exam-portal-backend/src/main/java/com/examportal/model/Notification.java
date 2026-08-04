package com.examportal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications",
        indexes = {
                @Index(name = "idx_notification_user",
                        columnList = "user_id"),
                @Index(name = "idx_notification_read",
                        columnList = "is_read")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Who receives this notification ────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ── Message ───────────────────────────────────────────────────
    @Column(nullable = false, length = 500)
    private String message;

    // ── Type: RESULT | APPROVAL | BLOCKED | SYSTEM | TIMER ───────
    @Column(nullable = false, length = 50)
    private String type;

    // ── Read status ───────────────────────────────────────────────
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    // ── Reference to attempt/exam ─────────────────────────────────
    @Column(name = "reference_id")
    private Long referenceId;

    // ── Timestamp ─────────────────────────────────────────────────
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}