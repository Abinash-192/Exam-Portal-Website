package com.examportal.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class SubmitAttemptRequest {

    @NotNull(message = "Exam ID is required")
    private Long examId;

    @NotNull(message = "Attempt ID is required")
    private Long attemptId;

    // questionId → selectedOptionId (null = skipped)
    private Map<Long, Long> answers;

    // Time taken in seconds from frontend countdown
    private int timeTakenSeconds;

    // COMPLETED | TIMED_OUT
    private String status = "COMPLETED";

    // Remaining seconds at submission
    private int remainingTimeSeconds;
}