package com.examportal.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartAttemptRequest {

    @NotNull(message = "Exam ID is required")
    private Long examId;

    private boolean cameraConsent;
    private boolean audioConsent;
}