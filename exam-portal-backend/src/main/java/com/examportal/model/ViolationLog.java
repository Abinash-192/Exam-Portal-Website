package com.examportal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "violation_logs",
        indexes = {
                @Index(name = "idx_violation_session",
                        columnList = "proctoring_session_id"),
                @Index(name = "idx_violation_type",
                        columnList = "violation_type"),
                @Index(name = "idx_violation_user",
                        columnList = "user_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViolationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proctoring_session_id",
            nullable = false)
    private ProctoringSession proctoringSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "violation_type", nullable = false)
    private ViolationType violationType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Severity: LOW | MEDIUM | HIGH | CRITICAL
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private Severity severity;

    // AI confidence score for detected violation
    @Column(name = "confidence_score")
    private Double confidenceScore;

    // Screenshot path / audio clip reference
    @Column(name = "evidence_url", length = 500)
    private String evidenceUrl;

    @CreationTimestamp
    @Column(name = "detected_at", updatable = false)
    private LocalDateTime detectedAt;

    public enum ViolationType {
        TAB_SWITCH,
        WINDOW_BLUR,
        FACE_NOT_DETECTED,
        MULTIPLE_FACES_DETECTED,
        INAPPROPRIATE_VOICE,
        COPY_PASTE_DETECTED,
        FULLSCREEN_EXIT,
        PHONE_DETECTED,
        LOOKING_AWAY,
        SUSPICIOUS_AUDIO,
        UNAUTHORIZED_PERSON
    }

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}