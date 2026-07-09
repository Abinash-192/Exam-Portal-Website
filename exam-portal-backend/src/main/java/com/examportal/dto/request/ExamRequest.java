package com.examportal.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ExamRequest {

    @NotBlank(message = "Exam title is required")
    @Size(min = 3,max = 200,message = "Title must be 3-200 chatracters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 50,message = "Category must not exceed 50 characters")
    private String category;

    @Min(value = 1,message = "Duration must be at least 1 minute")
    @Max(value = 180, message = "Duration cannot exceed 180 minutes")
    private int durationMintues;

    @Min(value = 1, message = "Total marks must be atleast 20")
    private int totalMarks;

    @Min(value = 1, message = "passing marks must be at least 10")
    private int passingMarks;

    @NotBlank(message = "Difficulty is required(easy/medium/hard)")
    private String difficulty;

    private boolean active;

    @Size(max = 3000,message = "Instuctions must not exceed 3000 characters")
    private String instructions;

    @Size(max = 500,message = "Thumbnail URL must not exceed 500 characters")
    private String thumbnailUrl;

    @Size(max = 300,message = "Tags must not exceed 300 characters")
    private String tags;

    @Valid
    private List<QuestionRequest> questions;
}
