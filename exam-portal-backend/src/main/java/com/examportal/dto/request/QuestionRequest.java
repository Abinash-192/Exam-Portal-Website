package com.examportal.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class QuestionRequest {

    @NotBlank(message = "Question content is required")
    @Size(min = 5, max = 2000, message = "Question must be 5-2000 characters")
     private  String content;

    @Size(max = 5000,  message = "code snippet max 5000 characters")
    private String codeSnippet;

    private String language;

    private String questionType;

    @Min(value = 1, message = "Marks must be at least 1")
    @Max(value = 10, message = "marks cannot exceed 10")
    private int marks = 1;

    @Min(value = 0,message = "Question order must be >= 0")
    private  int questionOrder;

    @NotEmpty(message = "At least 2 options are required")
    @Size(min = 2,max = 4, message = "provide 2 to 4 options")
    @Valid
    private List<OptionRequest> options;

    @Min(value = 0,message = "correctOptionIndex must be 0 or greater")
    private int correctOptionIndex;

    @Size(max = 2000, message = "Explanation max 2000 characters")
    private String explaination;

    private double negativeMarks = 0.25;

    private  boolean aiGenerated = false;

    @Data
    public static  class OptionRequest {

        @NotBlank(message = "Option label is required ")
        @Pattern(regexp = "^[A-Da-d]$", message = "Label must be A, B, C , D")
        private String optionLabel;

        @NotBlank(message = "Option text is required")
        @Size(min = 1, max = 1000, message = "Option text must be 1-1000 characters")
        private String optionText;

        @Size(max = 3000,
                message = "Option code must not exceed 3000 characters")
        private String optionCode;

    }
}
