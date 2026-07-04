package com.examportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExamStatsResponse {

    private Long   examId;
    private String examTitle;
    private String category;
    private String difficulty;

    // ── Attempt breakdown ─────────────────────────────────────────
    private long   totalAttempts;
    private long   totalPassed;
    private long   totalFailed;
    private double passRate;

    // ── Score analytics ───────────────────────────────────────────
    private int    totalMarks;
    private int    passingMarks;
    private double avgScore;
    private double avgPercentage;
    private int    highestScore;
    private int    lowestScore;
}