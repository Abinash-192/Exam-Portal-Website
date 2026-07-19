package com.examportal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exam_attempts",
        indexes = {
                @Index(name = "idx_attempt_user",
                        columnList = "user_id"),
                @Index(name = "idx_attempt_exam",
                        columnList = "exam_id"),
                @Index(name = "idx_attempt_status",
                        columnList = "status"),
                @Index(name = "idx_attempt_expires",
                        columnList = "expires_at")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relations ─────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    // ── Score ─────────────────────────────────────────────────────
    @Column(name = "score_obtained", nullable = false)
    @Builder.Default
    private int scoreObtained = 0;

    @Column(name = "total_marks", nullable = false)
    @Builder.Default
    private int totalMarks = 0;

    @Column(name = "correct_answers")
    @Builder.Default
    private int correctAnswers = 0;

    @Column(name = "wrong_answers")
    @Builder.Default
    private int wrongAnswers = 0;

    @Column(name = "unanswered")
    @Builder.Default
    private int unanswered = 0;

    @Column(name = "percentage")
    @Builder.Default
    private double percentage = 0.0;

    @Column(name = "passed", nullable = false)
    @Builder.Default
    private boolean passed = false;

    // ── Timer ─────────────────────────────────────────────────────
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "allowed_time_seconds")
    private int allowedTimeSeconds;

    @Column(name = "time_taken_seconds")
    @Builder.Default
    private int timeTakenSeconds = 0;

    @Column(name = "remaining_time_seconds")
    private int remainingTimeSeconds;

    // ── Status ────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    // ── Email tracking ────────────────────────────────────────────
    @Column(name = "result_email_sent")
    @Builder.Default
    private boolean resultEmailSent = false;

    @Column(name = "feedback_generated")
    @Builder.Default
    private boolean feedbackGenerated = false;

    // ── Auto-submit ───────────────────────────────────────────────
    @Column(name = "auto_submitted")
    @Builder.Default
    private boolean autoSubmitted = false;

    // ── Timestamps ────────────────────────────────────────────────
    @CreationTimestamp
    @Column(name = "attempted_at", updatable = false)
    private LocalDateTime attemptedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    // ── Answers ───────────────────────────────────────────────────
    @OneToMany(mappedBy      = "attempt",
            cascade       = CascadeType.ALL,
            orphanRemoval = true,
            fetch         = FetchType.LAZY)
    @Builder.Default
    private List<AttemptAnswer> answers = new ArrayList<>();

    public enum AttemptStatus {
        IN_PROGRESS,
        COMPLETED,
        TIMED_OUT,
        DISQUALIFIED
    }
}