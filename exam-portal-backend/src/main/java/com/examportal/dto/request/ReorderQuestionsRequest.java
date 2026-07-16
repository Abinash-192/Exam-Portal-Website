package com.examportal.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ReorderQuestionsRequest {

    @NotEmpty(message = "Question Ids list is required")
    private List<Long> orderedQuestionIds;
}
