package com.examportal.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionStatsResponse {

    private  Long  examId;
    private  String  examTitle;

    private  int  totalQuestions;
    private  int  totalMarks;

    private  int  javaQuestions;
    private  int  pythonQuestions;
    private  int  csharpQuestions;
    private  int  sqlQuestions;
    private  int  javascriptQuestions;
    private  int  generalQuestions;

    private  int  codeQuestions;
    private  int  conceptualQuestions;
    private  int  scenarioQuestions;
    private  int  behavioralQuestions;

    private  int  aiGeneratedCount;
    private  int  manualCount;
}
