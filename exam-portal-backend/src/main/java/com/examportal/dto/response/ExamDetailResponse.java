package com.examportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExamDetailResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private String difficulty;
    private String instructions;
    private String thumbnailUrl;
    private String tags;

    private int durationMintues;
    private int totalMarks;
    private int passingMarks;
    private int questionCount;

    private LocalDateTime createdAt;

    private List<QuestionResponse> questions;
}
