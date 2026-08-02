package com.examportal.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveAnswerRequest {

    @NotNull(message = "Attempt ID is required")
    private Long attemptId;

    @NotNull(message = "Question ID is required")
    private Long questionId;

    // null = unanswered / cleared
    private Long selectedOptionId;
}