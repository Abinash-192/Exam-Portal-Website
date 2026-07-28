package com.examportal.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attempt_answers",
        indexes = {
                @Index(name = "idx_answer_attempt",
                        columnList = "attempt_id"),
                @Index(name = "idx_answer_question",
                        columnList = "question_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Attempt this answer belongs to ────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private ExamAttempt attempt;

    // ── Question answered ─────────────────────────────────────────
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    // ── Selected option (null = skipped) ──────────────────────────
    @Column(name = "selected_option_id")
    private Long selectedOptionId;

    // ── Was the answer correct ────────────────────────────────────
    @Column(name = "is_correct", nullable = false)
    @Builder.Default
    private boolean correct = false;
}