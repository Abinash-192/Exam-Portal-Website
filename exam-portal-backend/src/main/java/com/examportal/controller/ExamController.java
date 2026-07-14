package com.examportal.controller;

import com.examportal.dto.request.ExamFilterRequest;
import com.examportal.dto.request.ExamRequest;
import com.examportal.dto.request.QuestionRequest;
import com.examportal.dto.response.*;
import com.examportal.repository.UserRepository;
import com.examportal.service.ExamAttemptService;
import com.examportal.service.ExamService;
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
@RequestMapping("api/exams")
public class ExamController {

    private final ExamService examService;
    private final QuestionService questionService;
    private final ExamAttemptService attemptService;
    private final UserRepository userRepository;


    @GetMapping()
    public ResponseEntity<ApiResponse<List<ExamSummaryResponse>>> getActiveExams(){

        return ok("Active exams fetched.",examService.getAllActiveExams());
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<ExamSummaryResponse>>>  filterExams(ExamFilterRequest filter){

        return ok("Filtered exams.",examService.filterExams(filter));
    }

    @GetMapping("/search")
    public  ResponseEntity<ApiResponse<List<ExamSummaryResponse>>>  searchExams(@RequestParam String keyword){

        return ok("Search results.", examService.searchActiveExams(keyword));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ExamSummaryResponse>>>  getByCategory(@PathVariable String category){

        return ok("Exams by category.",examService.getActiveByCategory(category));
    }

    @GetMapping("/difficulty/{difficulty}")
     public ResponseEntity<ApiResponse<List<ExamSummaryResponse>>>  getByDifficulty(@PathVariable String difficulty){

        return ok("Exams by difficulty.",examService.getActiveByDifficulty(difficulty));
     }

     @GetMapping("/categories")
     public ResponseEntity<ApiResponse<List<String>>>  getActiveCategories(){

         return ok("Active categories.",examService.getActiveCategories());
     }

     @GetMapping("/{id}")
     public ResponseEntity<ApiResponse<ExamDetailResponse>>  getExamForUser(@PathVariable Long id){

         return ok("Exam detail fetched.",examService.getExamForUser(id));
     }

     @GetMapping("/{id}/summary")
     public ResponseEntity<ApiResponse<ExamSummaryResponse>>  getExamSummary(@PathVariable Long id){

         return ok("Exam summary.",examService.getExamSummary(id));
     }

     @GetMapping("/category/{category}/count")
     public ResponseEntity<ApiResponse<Long>> countByCategory(@PathVariable String category){

         return ok("Exam count by category.",examService.countByCategoryAndActive(category));
     }

     @GetMapping("/count/active")
     public ResponseEntity<ApiResponse<Long>>  countActiveExams(){

         return ok("Active exam count.",examService.countActiveExams());
     }

     @GetMapping("/admin/all")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<List<ExamResponse>>>  getAllExamsAdmin(){

         return ok("All exams fetched (admin).",examService.getAllExamsAdmin());
     }

     @GetMapping("/admin/categories")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<List<String>>>  getAllCategoriesAdmin(){

         return ok("All categories.",examService.getAllCategories());
     }

     @GetMapping("/admin/count/all")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<Long>>  countAllExams(){

         return ok("Total exam count.", examService.countAllExams());
     }

     @GetMapping("/admin/{id}")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<ExamResponse>>  countExamForAdmin(@PathVariable Long id){

         return  ok("Exam detail (admin).",examService.getExamForAdmin(id));
     }

     @GetMapping("/admin/{id}/stats")
     @PreAuthorize("hasRole('ADMIN')")
     public  ResponseEntity<ApiResponse<ExamStatsResponse>>  getExamStats(@PathVariable Long id ){

         return ok("Exam analytics.",examService.getExamStats(id));
     }

     @GetMapping("/admin/{id}/results")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<List<AttemptResultResponse>>>  getExamResults(@PathVariable Long id){

        return ok("Exam results.",attemptService.getExamResults(id));
     }

     @PostMapping
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<ExamResponse>>  createExam(@Valid @RequestBody ExamRequest req){

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Exam created successfully.",examService.createExam(req)));
     }

     @PutMapping("/{id}")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<ExamResponse>>  updateExam(@PathVariable Long id,@Valid @RequestBody ExamRequest req){

         return ok("Exam updated successfully.",examService.updateExam(id, req));
     }

     @DeleteMapping("/{id}")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<Void>>  deleteExam(@PathVariable Long id){

         examService.deleteExam(id);
         return ok("Exam permanetly deleted.",null);
     }

     @PatchMapping("/{id}/toggle")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<ExamResponse<ExamSummaryResponse>>>  toggleStatus(@PathVariable Long id){

         return ok("Exam status toggled.",examService.toggleStatus(id));
     }

     @PatchMapping("/{id}/activate")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<ExamSummaryResponse>>  activeExam(@PathVariable Long id){

         return ok("Exam activated.",examService.activateExam(id));
     }

     @PatchMapping("/{id}/deactivate")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<ExamSummaryResponse>>  deactivateExam(@PathVariable Long id){

        return  ok("Exam deactivated.",examService.deactivateExam(id));
     }

     @GetMapping("/{examId}/questions")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<List<QuestionResponse>>>  getQuestionsAdmin(@PathVariable Long examId){

        return ok("Questions fetched with fetched.",questionService.getQuestionsForAdmin(examId));
     }

     @GetMapping("/{examId}/questions/summaries")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<List<QuestionSummaryResponse>>>  getQuestionSummaries(@PathVariable Long examId){

         return ok("Question summaries.",questionService.getQuestionSummaries(examId));
     }

     @GetMapping("/{examId}/questions/user")
     public ResponseEntity<ApiResponse<List<QuestionResponse>>>  getQuestionsForUser(@PathVariable Long examId){

        return ok("Questions for exam.",questionService.getQuestionsForUser(examId));
     }

     @GetMapping("/{examId}/questions/language/{language]")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<List<QuestionResponse>>>  getByLanguage(@PathVariable Long examId,@PathVariable String language){

         return ok("Questions by language.",questionService.getByLanguage(examId,language));
     }

     @GetMapping("/{examId}/questions/count")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<Integer>>  getQuestionCount(@PathVariable Long examId){

        return ok("Question count.",questionService.countByExam(examId));
     }

     @PostMapping("/{examId}/questions")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<QuestionResponse>>  addQuestion(@PathVariable Long examId, @Valid @RequestBody QuestionRequest req){

         return  ResponseEntity
                 .status(HttpStatus.CREATED)
                 .body(ApiResponse.Success("Question added.",questionService.addQuestion(examId,req)));
     }

     @PostMapping("/{examId}/questions/bulk")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<List<QuestionResponse>>>  addBulkQuestions(@PathVariable Long examId, @Valid @RequestBody BulkQuestionRequest req){

         return ResponseEntity.status(HttpStatus.CREATED)
                 .body(ApiResponse.success("Qestions added in bulk.",questionService.addBulkQuestions(examId,req)));
     }

     @PutMapping("/{examId}/questions/{questionId}")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<QuestionResponse>>  updateQuestion(@PathVariable Long examId,@PathVariable Long questionId, @Valid @RequestBody QuestionRequest req){

        return ok("Question updated.",questionService.updateQuestion(questionId,req));
     }

     @DeleteMapping("/{examId}/questions/{questionId}")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable Long examId,@PathVariable Long questionId){

         questionService.deleteQuestion(questionId);
         return ok("Question deleted.",null);
     }

     @PatchMapping("/{examId}/questions/reorder")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<List<QuestionSummaryResponse>>>  reorderQuestions(@PathVariable Long examId,
                                                                                         @Valid @RequestBody ReorderQuestionRequest req){

         return  ok("Questions reordered.",questionService.reorderQuestions(examId,req));
     }

     @GetMapping("/questions/admin/{questionId}")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<QuestionResponse>>  getQuestionById(@PathVariable Long questionId){

         return ok("Question fetched.",questionService.getQuestionById(questionId));
     }

     @PostMapping("/{examId}/results/{attemptId}/resend-email")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ApiResponse<Void>>  resendResultEmail(@PathVariable Long examId, @PathVariable Long attemptId){

         attemptService.resendResultEmail(attemptId);
         return  ok("Result email re-dispatched.", null);
     }

     private <T> ResponseEntity<ApiResponse<T>>  ok(String message , T data){

         return  ResponseEntity.ok(ApiResponse.success(message,data));
     }
}
