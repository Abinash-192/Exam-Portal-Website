package com.examportal.dto.request;

import lombok.Data;

@Data
public class ExamFilterRequest {

    private String category;

    private String difficulty;

    private String keyword;

    private Boolean activeOnly;
}
