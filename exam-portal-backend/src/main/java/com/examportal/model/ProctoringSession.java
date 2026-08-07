package com.examportal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proctoring_sessions",
        indexes = {
                @Index(name = "idx_proctor_attempt",
                        columnList = "attempt_id"),
                @Index(name = "idx_proctor_user",
                        columnList = "user_id"),
                @Index(name = "idx_proctor_status",
                        columnList = "proctoring_status")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProctoringSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private ExamAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ACTIVE | FLAGGED | DISQUALIFIED | COMPLETED | CLEAN
    @Enumerated(EnumType.STRING)
    @Column(name = "proctoring_status", nullable = false)
    @Builder.Default
    private ProctoringStatus proctoringStatus =
            ProctoringStatus.ACTIVE;

    // ── Violation counts ──────────────────────────────────────────
    @Column(name = "tab_switch_count")
    @Builder.Default
    private int tabSwitchCount = 0;

    @Column(name = "face_not_detected_count")
    @Builder.Default
    private int faceNotDetectedCount = 0;

    @Column(name = "multiple_faces_count")
    @Builder.Default
    private int multipleFacesCount = 0;

    @Column(name = "inappropriate_voice_count")
    @Builder.Default
    private int inappropriateVoiceCount = 0;

    @Column(name = "copy_paste_count")
    @Builder.Default
    private int copyPasteCount = 0;

    @Column(name = "window_blur_count")
    @Builder.Default
    private int windowBlurCount = 0;

    @Column(name = "fullscreen_exit_count")
    @Builder.Default
    private int fullscreenExitCount = 0;

    @Column(name = "total_violations")
    @Builder.Default
    private int totalViolations = 0;

    // ── Disqualification ─────────────────────────────────────────
    @Column(name = "is_disqualified")
    @Builder.Default
    private boolean disqualified = false;

    @Column(name = "disqualification_reason",
            columnDefinition = "TEXT")
    private String disqualificationReason;

    @Column(name = "disqualified_at")
    private LocalDateTime disqualifiedAt;

    // ── Camera / Audio status ─────────────────────────────────────
    @Column(name = "camera_enabled")
    @Builder.Default
    private boolean cameraEnabled = false;

    @Column(name = "audio_enabled")
    @Builder.Default
    private boolean audioEnabled = false;

    @Column(name = "screen_share_enabled")
    @Builder.Default
    private boolean screenShareEnabled = false;

    // ── Admin notified ────────────────────────────────────────────
    @Column(name = "admin_notified")
    @Builder.Default
    private boolean adminNotified = false;

    // ── Timestamps ────────────────────────────────────────────────
    @CreationTimestamp
    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    // ── Violation log entries ─────────────────────────────────────
    @OneToMany(mappedBy = "proctoringSession",
            cascade  = CascadeType.ALL,
            fetch    = FetchType.LAZY)
    @Builder.Default
    private List<ViolationLog> violations = new ArrayList<>();

    public enum ProctoringStatus {
        ACTIVE, FLAGGED, DISQUALIFIED, COMPLETED, CLEAN
    }
}