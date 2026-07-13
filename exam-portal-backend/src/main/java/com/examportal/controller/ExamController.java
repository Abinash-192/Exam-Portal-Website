package com.examportal.controller;

import com.examportal.dto.request.ExamFilterRequest;
import com.examportal.dto.response.ApiResponse;
import com.examportal.dto.response.ExamDetailResponse;
import com.examportal.dto.response.ExamResponse;
import com.examportal.dto.response.ExamSummaryResponse;
import com.examportal.repository.UserRepository;
import com.examportal.service.ExamAttemptService;
import com.examportal.service.ExamService;
import com.examportal.service.QuestionService;
import lombok.RequiredArgsConstructor;
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
}
