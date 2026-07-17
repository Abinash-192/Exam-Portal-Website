package com.examportal.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionSummaryResponse {

     private Long id;
     private String content;
     private String language;
     private String questionType;
     private int marks;
     private double negativeMarks;
     private int questionOrder;
     private int optionCount;
     private boolean aiGenerated;
}
