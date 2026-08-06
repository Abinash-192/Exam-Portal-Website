package com.examportal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interview_sessions",
        indexes = {
                @Index(name = "idx_interview_user",
                        columnList = "user_id"),
                @Index(name = "idx_interview_category",
                        columnList = "category"),
                @Index(name = "idx_interview_status",
                        columnList = "status")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // java | python | csharp | sql | javascript | general
    @Column(nullable = false, length = 50)
    private String category;

    // BEGINNER | INTERMEDIATE | ADVANCED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionLevel level;

    // ACTIVE | COMPLETED | ABANDONED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "total_questions")
    @Builder.Default
    private int totalQuestions = 0;

    @Column(name = "answered_questions")
    @Builder.Default
    private int answeredQuestions = 0;

    @Column(name = "score_percentage")
    private Double scorePercentage;

    @Column(name = "session_feedback", columnDefinition = "TEXT")
    private String sessionFeedback;

    @CreationTimestamp
    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "session",
            cascade  = CascadeType.ALL,
            fetch    = FetchType.LAZY)
    @Builder.Default
    private List<InterviewQA> qaList = new ArrayList<>();

    public enum SessionLevel { BEGINNER, INTERMEDIATE, ADVANCED }
    public enum SessionStatus { ACTIVE, COMPLETED, ABANDONED }
}