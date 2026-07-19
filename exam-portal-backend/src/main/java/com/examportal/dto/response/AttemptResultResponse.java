package com.examportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttemptResultResponse {

    // ── Attempt meta ──────────────────────────────────────────────
    private Long   attemptId;
    private String examTitle;
    private String category;
    private String difficulty;
    private String status;

    // ── Timer ─────────────────────────────────────────────────────
    private int    allowedTimeSeconds;
    private int    timeTakenSeconds;
    private int    remainingTimeSeconds;
    private String timeTakenFormatted;
    private boolean autoSubmitted;

    // ── Score ─────────────────────────────────────────────────────
    private int    scoreObtained;
    private int    totalMarks;
    private double percentage;
    private boolean passed;
    private int    passingMarks;

    // ── Breakdown ─────────────────────────────────────────────────
    private int correctAnswers;
    private int wrongAnswers;
    private int unanswered;

    // ── Performance ───────────────────────────────────────────────
    private String performanceBand;
    private double avgScoreForExam;

    // ── Timestamps ────────────────────────────────────────────────
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime attemptedAt;

    // ── Per-question review ───────────────────────────────────────
    private List<AnswerDetail> answerDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AnswerDetail {
        private Long    questionId;
        private String  questionText;
        private String  codeSnippet;
        private String  language;
        private Long    selectedOptionId;
        private String  selectedOptionText;
        private Long    correctOptionId;
        private String  correctOptionText;
        private boolean correct;
        private int     marks;
        private String  explanation;
    }
}