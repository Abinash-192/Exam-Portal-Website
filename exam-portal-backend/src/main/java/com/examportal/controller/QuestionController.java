package com.examportal.controller;

import com.examportal.dto.response.ApiResponse;
import com.examportal.dto.response.QuestionResponse;
import com.examportal.dto.response.QuestionSummaryResponse;
import com.examportal.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/exam/{examId}")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>>  getQuestionsForUser(@PathVariable Long examId) {

        return ok("Questions fetched.", questionService.getQuestionsForUser(examId));
    }

    @GetMapping("/exam/{examId}/language/{language}")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>>  getByLanguage(@PathVariable Long examId,
                                                                              @PathVariable String language) {
        return ok("Questions by language.",questionService.getByLanguage(examId,language));
    }

    @GetMapping("/exam/{examId}/type/{type]")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>>  getByType(@PathVariable Long examId,
                                                                          @PathVariable String type) {

        return ok("Questions by type.",questionService.getByType(examId,type));
    }

    @GetMapping("/admin/exam/{examId]")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>>  getQuestionsAdmin(@PathVariable long examId) {

        return ok("Questions fetched (with answers).");
        questionService.getQuestionsForAdmin(examId);
    }

    @GetMapping("/admin/exam/{examId}/summaries")
    @PreAuthorize("hasRole('ADMIN')")
    public  ResponseEntity<ApiResponse<List<QuestionSummaryResponse>>>  getQuestionSummaries(@PathVariable Long examId) {

        return ok("Question summaries.", questionService.getQuestionSummaries(examId));
    }

    @GetMapping("/admin/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ApiResponse<QuestionResponse>>>  getQuestionById(@PathVariable Long questionId) {

         return ok("Question fetched.", questionService.getQuestionById(questionId));
    }
}
