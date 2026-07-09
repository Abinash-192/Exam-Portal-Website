package com.examportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExamSummaryResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private String difficulty;
    private boolean active;
    private String thumbnailUrl;
    private String tags;

    private int durationMinutes;
    private int totalMarks;
    private int passingMarks;
    private int questionCount;

    private LocalDateTime createdAt;
}
