package com.examportal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_qa",
        indexes = {
                @Index(name = "idx_qa_session",
                        columnList = "session_id"),
                @Index(name = "idx_qa_category",
                        columnList = "category")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewQA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private InterviewSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    // AI-generated ideal answer
    @Column(name = "ideal_answer",
            columnDefinition = "TEXT")
    private String idealAnswer;

    // Student's answer (typed or voice-transcribed)
    @Column(name = "user_answer",
            columnDefinition = "TEXT")
    private String userAnswer;

    // AI evaluation of student's answer
    @Column(name = "ai_evaluation",
            columnDefinition = "TEXT")
    private String aiEvaluation;

    // AI score 0-100
    @Column(name = "answer_score")
    private Integer answerScore;

    // Tips to improve
    @Column(name = "improvement_tips",
            columnDefinition = "TEXT")
    private String improvementTips;

    // CODE | CONCEPTUAL | SCENARIO | BEHAVIORAL
    @Column(name = "question_type", length = 20)
    private String questionType;

    @Column(name = "difficulty", length = 10)
    private String difficulty;

    // Was the question bookmarked for review
    @Column(name = "bookmarked")
    @Builder.Default
    private boolean bookmarked = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}