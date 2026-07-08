package com.examportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExamResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private String difficulty;
    private boolean active;
    private String instructions;
    private String thumbnail;
    private String tags;

    private int durationMintues;
    private int totalMarks;
    private int passingMarks;
    private int questionCount;

    private Long totalAttempts;
    private Long totalPassed;
    private Double avgscore;
    private Double avgPecentage;
    private Double passRate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<QuestionResponse>  questions;
}
