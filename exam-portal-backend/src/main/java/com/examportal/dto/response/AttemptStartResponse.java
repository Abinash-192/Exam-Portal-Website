package com.examportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttemptStartResponse {

    private Long   attemptId;
    private Long   examId;
    private String examTitle;
    private String category;
    private String difficulty;
    private String instructions;

    // ── Timer ─────────────────────────────────────────────────────
    private int           durationMinutes;
    private int           allowedTimeSeconds;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private String        serverTime;

    // ── Exam info ─────────────────────────────────────────────────
    private int    totalMarks;
    private int    passingMarks;
    private int    questionCount;

    // ── Questions without correct answers ─────────────────────────
    private List<QuestionResponse> questions;

    // ── Status ────────────────────────────────────────────────────
    private String  status;
    private boolean proctoringRequired;
}