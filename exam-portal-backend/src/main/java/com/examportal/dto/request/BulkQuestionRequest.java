package com.examportal.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkQuestionRequest {

    @NotEmpty(message = "At least one question is required")
    @Valid
    private List<QuestionRequest> questions;
}
