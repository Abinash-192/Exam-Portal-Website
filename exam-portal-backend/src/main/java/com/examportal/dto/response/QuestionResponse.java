package com.examportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionResponse {

    private Long    id;
    private String  content;
    private String  codeSnippet;
    private String  language;
    private String  questionType;
    private int     marks;
    private double  negativeMarks;
    private int     questionOrder;
    private boolean aiGenerated;

    // Only for admin / result review — null for students
    private Long   correctOptionId;
    private String explanation;

    private List<OptionResponse> options;

    // ── Nested Option ─────────────────────────────────────────────
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OptionResponse {
        private Long   id;
        private String optionLabel;
        private String optionText;
        private String optionCode;
    }
}