package com.examportal.controller;

import com.examportal.dto.request.BulkQuestionRequest;
import com.examportal.dto.request.QuestionFilterRequest;
import com.examportal.dto.request.QuestionRequest;
import com.examportal.dto.request.ReorderQuestionsRequest;
import com.examportal.dto.response.ApiResponse;
import com.examportal.dto.response.QuestionResponse;
import com.examportal.dto.response.QuestionStatsResponse;
import com.examportal.dto.response.QuestionSummaryResponse;
import com.examportal.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

        return ok("Questions fetched (with answers)." , questionService.getQuestionsForAdmin(examId));
    }

    @GetMapping("/admin/exam/{examId}/summaries")
    @PreAuthorize("hasRole('ADMIN')")
    public  ResponseEntity<ApiResponse<List<QuestionSummaryResponse>>>  getQuestionSummaries(@PathVariable Long examId) {

        return ok("Question summaries.", questionService.getQuestionSummaries(examId));
    }

    @GetMapping("/admin/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestionResponse>>  getQuestionById(@PathVariable Long questionId) {

         return ok("Question fetched.", questionService.getQuestionById(questionId));
    }

    @GetMapping("/admin/exam/{examId}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestionStatsResponse>>  getQuestionStats(@PathVariable Long examId) {

         return  ok("Question stats.", questionService.getQuestionStats(examId));
    }

    @GetMapping("/admin/exam/{examId}/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>>  getCount(@PathVariable Long examId) {

        return ok("Question count.", questionService.countByExam(examId));
    }

    @GetMapping("/admin/exam/{examId}/total-marks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>>  getTotalMarks(@PathVariable Long examId) {

        return ok("Total marks sum.", questionService.sumMarksByExam(examId));
    }

    @GetMapping("/admin/exam/{examId}/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>>  filterQuestions(@PathVariable Long examId,
                                                                                QuestionFilterRequest filter) {
        return ok("Filtered questions.", questionService.filterQuestions(examId,filter,true));
    }

    @GetMapping("/admin/exam/{examId}/language-type")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>>  getByLanguageAndType(@PathVariable Long examId,
                                                                                      @RequestParam String language,
                                                                                      @RequestParam String type) {

        return ok("Questions by language and type.",questionService.getByLanguageAndType(examId, language,type));
    }

    @PostMapping("/exam/{examId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestionResponse>> addQuestion(@PathVariable Long examId,
                                                                     @Valid @RequestBody QuestionRequest req) {

         return ResponseEntity.status(HttpStatus.CREATED)
                 .body(ApiResponse.created("Question added successfully.", questionService.addQuestion(examId, req)));
    }

    @PostMapping("/exam/{examId}/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>>  addBulkQuestions(@PathVariable long examId, @Valid @RequestBody BulkQuestionRequest req) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Questions added in bulk.",questionService.addBulkQuestions(examId,req)));
    }

    @PutMapping("/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestionResponse>>  updateQuestion(@PathVariable Long questionId,
                                                                         @Valid @RequestBody QuestionRequest req) {

        return ok("Question updated.", questionService.updateQuestion(questionId,req));
    }

    @DeleteMapping("/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable Long questionId) {

        questionService.deleteQuestion(questionId);
        return  ok("Question deleted.", null);
    }

    @PatchMapping("/exam/{examId}/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<QuestionSummaryResponse>>>  reorderQuestions(@PathVariable Long examId,
                                                                                        @Valid @RequestBody ReorderQuestionsRequest req) {

        return  ok("Question reordered.",questionService.reorderQuestions(examId,req));
    }

    private  <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {

        return ResponseEntity.ok(ApiResponse.success(message,data));
    }
}
