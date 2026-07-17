package com.examportal.dto.request;

import lombok.Data;

@Data
public class QuestionFilterRequest {

     private String language;

     private String questionType;

     private String keyword;

     private Boolean aiGenerated;
}
