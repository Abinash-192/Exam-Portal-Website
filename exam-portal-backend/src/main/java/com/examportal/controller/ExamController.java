//package com.examportal.controller;
//
//import com.examportal.dto.request.ExamFilterRequest;
//import com.examportal.dto.request.ExamRequest;
//import com.examportal.dto.request.QuestionRequest;
//import com.examportal.dto.response.*;
//import com.examportal.repository.UserRepository;
//import com.examportal.service.ExamAttemptService;
//import com.examportal.service.ExamService;
//import com.examportal.service.QuestionService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("api/exams")
//public class ExamController {
//
//    private final ExamService examService;
//    private final QuestionService questionService;
//    private final ExamAttemptService attemptService;
//    private final UserRepository userRepository;
//
//
//    @GetMapping()
//    public ResponseEntity<ApiResponse<List<ExamSummaryResponse>>> getActiveExams(){
//
//        return ok("Active exams fetched.",examService.getAllActiveExams());
//    }
//
//    @GetMapping("/filter")
//    public ResponseEntity<ApiResponse<List<ExamSummaryResponse>>>  filterExams(ExamFilterRequest filter){
//
//        return ok("Filtered exams.",examService.filterExams(filter));
//    }
//
//    @GetMapping("/search")
//    public  ResponseEntity<ApiResponse<List<ExamSummaryResponse>>>  searchExams(@RequestParam String keyword){
//
//        return ok("Search results.", examService.searchActiveExams(keyword));
//    }
//
//    @GetMapping("/category/{category}")
//    public ResponseEntity<ApiResponse<List<ExamSummaryResponse>>>  getByCategory(@PathVariable String category){
//
//        return ok("Exams by category.",examService.getActiveByCategory(category));
//    }
//
//    @GetMapping("/difficulty/{difficulty}")
//     public ResponseEntity<ApiResponse<List<ExamSummaryResponse>>>  getByDifficulty(@PathVariable String difficulty){
//
//        return ok("Exams by difficulty.",examService.getActiveByDifficulty(difficulty));
//     }
//
//     @GetMapping("/categories")
//     public ResponseEntity<ApiResponse<List<String>>>  getActiveCategories(){
//
//         return ok("Active categories.",examService.getActiveCategories());
//     }
//
//     @GetMapping("/{id}")
//     public ResponseEntity<ApiResponse<ExamDetailResponse>>  getExamForUser(@PathVariable Long id){
//
//         return ok("Exam detail fetched.",examService.getExamForUser(id));
//     }
//
//     @GetMapping("/{id}/summary")
//     public ResponseEntity<ApiResponse<ExamSummaryResponse>>  getExamSummary(@PathVariable Long id){
//
//         return ok("Exam summary.",examService.getExamSummary(id));
//     }
//
//     @GetMapping("/category/{category}/count")
//     public ResponseEntity<ApiResponse<Long>> countByCategory(@PathVariable String category){
//
//         return ok("Exam count by category.",examService.countByCategoryAndActive(category));
//     }
//
//     @GetMapping("/count/active")
//     public ResponseEntity<ApiResponse<Long>>  countActiveExams(){
//
//         return ok("Active exam count.",examService.countActiveExams());
//     }
//
//     @GetMapping("/admin/all")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<List<ExamResponse<E>>>>  getAllExamsAdmin(){
//
//         return ok("All exams fetched (admin).",examService.getAllExamsAdmin());
//     }
//
//     @GetMapping("/admin/categories")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<List<String>>>  getAllCategoriesAdmin(){
//
//         return ok("All categories.",examService.getAllCategories());
//     }
//
//     @GetMapping("/admin/count/all")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<Long>>  countAllExams(){
//
//         return ok("Total exam count.", examService.countAllExams());
//     }
//
//     @GetMapping("/admin/{id}")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<ExamResponse<E>>>  countExamForAdmin(@PathVariable Long id){
//
//         return  ok("Exam detail (admin).",examService.getExamForAdmin(id));
//     }
//
//     @GetMapping("/admin/{id}/stats")
//     @PreAuthorize("hasRole('ADMIN')")
//     public  ResponseEntity<ApiResponse<ExamStatsResponse>>  getExamStats(@PathVariable Long id ){
//
//         return ok("Exam analytics.",examService.getExamStats(id));
//     }
//
//     @GetMapping("/admin/{id}/results")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<List<AttemptResultResponse>>>  getExamResults(@PathVariable Long id){
//
//        return ok("Exam results.",attemptService.getExamResults(id));
//     }
//
//     @PostMapping
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<ExamResponse<E>>>  createExam(@Valid @RequestBody ExamRequest req){
//
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(ApiResponse.success("Exam created successfully.",examService.createExam(req)));
//     }
//
//     @PutMapping("/{id}")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<ExamResponse<E>>>  updateExam(@PathVariable Long id, @Valid @RequestBody ExamRequest req){
//
//         return ok("Exam updated successfully.",examService.updateExam(id, req));
//     }
//
//     @DeleteMapping("/{id}")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<Void>>  deleteExam(@PathVariable Long id){
//
//         examService.deleteExam(id);
//         return ok("Exam permanetly deleted.",null);
//     }
//
//     @PatchMapping("/{id}/toggle")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<ExamSummaryResponse>> toggleStatus(@PathVariable Long id){
//
//         return ok("Exam status toggled.",examService.toggleStatus(id));
//     }
//
//     @PatchMapping("/{id}/activate")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<ExamSummaryResponse>>  activeExam(@PathVariable Long id){
//
//         return ok("Exam activated.",examService.activateExam(id));
//     }
//
//     @PatchMapping("/{id}/deactivate")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<ExamSummaryResponse>>  deactivateExam(@PathVariable Long id){
//
//        return  ok("Exam deactivated.",examService.deactivateExam(id));
//     }
//
//     @GetMapping("/{examId}/questions")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<List<QuestionResponse>>>  getQuestionsAdmin(@PathVariable Long examId){
//
//        return ok("Questions fetched with fetched.",questionService.getQuestionsForAdmin(examId));
//     }
//
//     @GetMapping("/{examId}/questions/summaries")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<List<QuestionSummaryResponse>>>  getQuestionSummaries(@PathVariable Long examId){
//
//         return ok("Question summaries.",questionService.getQuestionSummaries(examId));
//     }
//
//     @GetMapping("/{examId}/questions/user")
//     public ResponseEntity<ApiResponse<List<QuestionResponse>>>  getQuestionsForUser(@PathVariable Long examId){
//
//        return ok("Questions for exam.",questionService.getQuestionsForUser(examId));
//     }
//
//     @GetMapping("/{examId}/questions/language/{language]")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<List<QuestionResponse>>>  getByLanguage(@PathVariable Long examId,@PathVariable String language){
//
//         return ok("Questions by language.",questionService.getByLanguage(examId,language));
//     }
//
//     @GetMapping("/{examId}/questions/count")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<Integer>>  getQuestionCount(@PathVariable Long examId){
//
//        return ok("Question count.",questionService.countByExam(examId));
//     }
//
//     @PostMapping("/{examId}/questions")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<QuestionResponse>>  addQuestion(@PathVariable Long examId, @Valid @RequestBody QuestionRequest req){
//
//         return  ResponseEntity
//                 .status(HttpStatus.CREATED)
//                 .body(ApiResponse.Success("Question added.",questionService.addQuestion(examId,req)));
//     }
//
//     @PostMapping("/{examId}/questions/bulk")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<List<QuestionResponse>>>  addBulkQuestions(@PathVariable Long examId, @Valid @RequestBody BulkQuestionRequest req){
//
//         return ResponseEntity.status(HttpStatus.CREATED)
//                 .body(ApiResponse.success("Qestions added in bulk.",questionService.addBulkQuestions(examId,req)));
//     }
//
//     @PutMapping("/{examId}/questions/{questionId}")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<QuestionResponse>>  updateQuestion(@PathVariable Long examId,@PathVariable Long questionId, @Valid @RequestBody QuestionRequest req){
//
//        return ok("Question updated.",questionService.updateQuestion(questionId,req));
//     }
//
//     @DeleteMapping("/{examId}/questions/{questionId}")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable Long examId,@PathVariable Long questionId){
//
//         questionService.deleteQuestion(questionId);
//         return ok("Question deleted.",null);
//     }
//
//     @PatchMapping("/{examId}/questions/reorder")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<List<QuestionSummaryResponse>>>  reorderQuestions(@PathVariable Long examId,
//                                                                                         @Valid @RequestBody ReorderQuestionRequest req){
//
//         return  ok("Questions reordered.",questionService.reorderQuestions(examId,req));
//     }
//
//     @GetMapping("/questions/admin/{questionId}")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<QuestionResponse>>  getQuestionById(@PathVariable Long questionId){
//
//         return ok("Question fetched.",questionService.getQuestionById(questionId));
//     }
//
//     @PostMapping("/{examId}/results/{attemptId}/resend-email")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<ApiResponse<Void>>  resendResultEmail(@PathVariable Long examId, @PathVariable Long attemptId){
//
//         attemptService.resendResultEmail(attemptId);
//         return  ok("Result email re-dispatched.", null);
//     }
//
//     private <T> ResponseEntity<ApiResponse<T>>  ok(String message , T data){
//
//         return  ResponseEntity.ok(ApiResponse.success(message,data));
//     }
//}


package com.examportal.controller;

import com.examportal.dto.request.ExamFilterRequest;
import com.examportal.dto.request.ExamRequest;
import com.examportal.dto.request.QuestionRequest;
import com.examportal.dto.request.BulkQuestionRequest;
import com.examportal.dto.request.ReorderQuestionsRequest;
import com.examportal.dto.response.*;
import com.examportal.model.User;
import com.examportal.repository.UserRepository;
import com.examportal.service.ExamAttemptService;
import com.examportal.service.ExamService;
import com.examportal.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService        examService;
    private final QuestionService    questionService;
    private final ExamAttemptService attemptService;
    private final UserRepository     userRepository;

    // ═════════════════════════════════════════════════════════════
    // USER FACING — Exam Listing & Discovery
    // ═════════════════════════════════════════════════════════════

    /**
     * GET /api/exams
     * Fetch all ACTIVE exams as summary cards.
     * Used in student exam listing page.
     * No questions, no correct answers included.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ExamSummaryResponse>>>
    getAllActiveExams() {
        return ok("Active exams fetched.",
                examService.getAllActiveExams());
    }

    /**
     * GET /api/exams/filter
     * Combined filter — category + difficulty + keyword.
     *
     * Query params:
     *   category   = Java | Python | C# | SQL | JavaScript | General
     *   difficulty = EASY | MEDIUM | HARD
     *   keyword    = search term (title / description / tags)
     *   activeOnly = true (default) | false
     *
     * Example: GET /api/exams/filter?category=Java&difficulty=EASY
     */
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<ExamSummaryResponse>>>
    filterExams(ExamFilterRequest filter) {
        return ok("Filtered exams.",
                examService.filterExams(filter));
    }

    /**
     * GET /api/exams/search?keyword=java
     * Full-text search across title, description, and tags.
     * Only searches active exams.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ExamSummaryResponse>>>
    searchExams(@RequestParam String keyword) {
        return ok("Search results.",
                examService.searchActiveExams(keyword));
    }

    /**
     * GET /api/exams/category/{category}
     * Active exams filtered by a specific category.
     * Categories: Java | Python | C# | SQL | JavaScript | General
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ExamSummaryResponse>>>
    getByCategory(@PathVariable String category) {
        return ok("Exams by category.",
                examService.getActiveByCategory(category));
    }

    /**
     * GET /api/exams/difficulty/{difficulty}
     * Active exams filtered by difficulty.
     * Difficulty values: EASY | MEDIUM | HARD
     */
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<ApiResponse<List<ExamSummaryResponse>>>
    getByDifficulty(@PathVariable String difficulty) {
        return ok("Exams by difficulty.",
                examService.getActiveByDifficulty(difficulty));
    }

    /**
     * GET /api/exams/categories
     * Distinct list of active exam categories.
     * Used to populate category filter dropdown for students.
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>>
    getActiveCategories() {
        return ok("Active categories.",
                examService.getActiveCategories());
    }

    /**
     * GET /api/exams/{id}
     * Full exam detail for student.
     * Includes all questions WITHOUT correct answers.
     * Used when student views exam details before starting.
     * Throws 400 if exam is inactive.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamDetailResponse>>
    getExamForUser(@PathVariable Long id) {
        return ok("Exam detail fetched.",
                examService.getExamForUser(id));
    }

    /**
     * GET /api/exams/{id}/summary
     * Lightweight exam summary for student — no questions included.
     * Used in exam card / listing UI.
     */
    @GetMapping("/{id}/summary")
    public ResponseEntity<ApiResponse<ExamSummaryResponse>>
    getExamSummary(@PathVariable Long id) {
        return ok("Exam summary.",
                examService.getExamSummary(id));
    }

    /**
     * GET /api/exams/category/{category}/count
     * Count of active exams in a specific category.
     * Used for category badge in UI.
     */
    @GetMapping("/category/{category}/count")
    public ResponseEntity<ApiResponse<Long>>
    countByCategory(@PathVariable String category) {
        return ok("Exam count by category.",
                examService.countByCategoryAndActive(category));
    }

    /**
     * GET /api/exams/count/active
     * Total count of all active exams.
     * Used in student dashboard stats.
     */
    @GetMapping("/count/active")
    public ResponseEntity<ApiResponse<Long>>
    countActiveExams() {
        return ok("Active exam count.",
                examService.countActiveExams());
    }

    // ═════════════════════════════════════════════════════════════
    // ADMIN FACING — Exam Management
    // ═════════════════════════════════════════════════════════════

    /**
     * GET /api/exams/admin/all
     * All exams including INACTIVE — with admin stats.
     * Each exam includes:
     *   totalAttempts, totalPassed, avgScore, passRate
     * Used in admin exam management table.
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ExamResponse>>>
    getAllExamsAdmin() {
        return ok("All exams fetched (admin).",
                examService.getAllExamsAdmin());
    }

    /**
     * GET /api/exams/admin/categories
     * All distinct categories including inactive exams.
     * Used to populate category dropdown in admin panel.
     */
    @GetMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<String>>>
    getAllCategoriesAdmin() {
        return ok("All categories.",
                examService.getAllCategories());
    }

    /**
     * GET /api/exams/admin/count/all
     * Total exam count (active + inactive).
     * Used in admin dashboard stats.
     */
    @GetMapping("/admin/count/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>>
    countAllExams() {
        return ok("Total exam count.",
                examService.countAllExams());
    }

    /**
     * GET /api/exams/admin/{id}
     * Full exam detail for admin view.
     * Includes:
     *   - All questions WITH correct answers & explanations
     *   - Admin stats: totalAttempts, passRate, avgScore
     * Used in popup edit modal & exam detail page.
     */
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExamResponse>>
    getExamForAdmin(@PathVariable Long id) {
        return ok("Exam detail (admin).",
                examService.getExamForAdmin(id));
    }

    /**
     * GET /api/exams/admin/{id}/stats
     * Per-exam analytics dashboard.
     * Returns:
     *   totalAttempts, totalPassed, totalFailed,
     *   passRate (%), avgScore, avgPercentage,
     *   highestScore, lowestScore
     */
    @GetMapping("/admin/{id}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExamStatsResponse>>
    getExamStats(@PathVariable Long id) {
        return ok("Exam analytics.",
                examService.getExamStats(id));
    }

    /**
     * GET /api/exams/admin/{id}/results
     * All student attempt results for a specific exam.
     * Admin uses this to review student performance.
     * Each result: userName, score, percentage, passed,
     *   correctAnswers, timeTaken, performanceBand
     */
    @GetMapping("/admin/{id}/results")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AttemptResultResponse>>>
    getExamResults(@PathVariable Long id) {
        return ok("Exam results.",
                attemptService.getExamResults(id));
    }

    // ═════════════════════════════════════════════════════════════
    // ADMIN FACING — Exam CRUD
    // ═════════════════════════════════════════════════════════════

    /**
     * POST /api/exams
     * Create a new exam.
     *
     * Required fields:
     *   title, category, durationMinutes,
     *   totalMarks, passingMarks, difficulty
     *
     * Optional:
     *   description, instructions, thumbnailUrl, tags
     *   questions[] — embed on create
     *
     * Validations:
     *   - Title must be unique (case-insensitive)
     *   - passingMarks <= totalMarks
     *   - durationMinutes: 1–180
     *   - difficulty: EASY | MEDIUM | HARD
     *
     * Returns HTTP 201 Created.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExamResponse>>
    createExam(@Valid @RequestBody ExamRequest req) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Exam created successfully.",
                        examService.createExam(req)));
    }

    /**
     * PUT /api/exams/{id}
     * Update exam — triggered from popup edit modal.
     *
     * Editable fields:
     *   title, description, category,
     *   durationMinutes (exam timing),
     *   totalMarks, passingMarks,
     *   difficulty, active,
     *   instructions, tags, thumbnailUrl
     *
     * Validations:
     *   - Title unique (excluding current exam id)
     *   - passingMarks <= totalMarks
     *
     * Action is audit-logged.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExamResponse>>
    updateExam(@PathVariable Long id,
               @Valid @RequestBody ExamRequest req) {
        return ok("Exam updated successfully.",
                examService.updateExam(id, req));
    }

    /**
     * DELETE /api/exams/{id}
     * Permanently delete an exam.
     *
     * Cascade deletes:
     *   - All questions and options
     *   - All attempt records and attempt answers
     *
     * Action is audit-logged.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>>
    deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ok("Exam permanently deleted.", null);
    }

    /**
     * PATCH /api/exams/{id}/toggle
     * Toggle exam between ACTIVE and INACTIVE.
     *
     * Active   = visible to students, can be attempted
     * Inactive = hidden from students
     *
     * Action is audit-logged.
     */
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExamSummaryResponse>>
    toggleStatus(@PathVariable Long id) {
        return ok("Exam status toggled.",
                examService.toggleStatus(id));
    }

    /**
     * PATCH /api/exams/{id}/activate
     * Explicitly activate an exam.
     * Convenience endpoint (same result as toggle when inactive).
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExamSummaryResponse>>
    activateExam(@PathVariable Long id) {
        return ok("Exam activated.",
                examService.activateExam(id));
    }

    /**
     * PATCH /api/exams/{id}/deactivate
     * Explicitly deactivate (hide) an exam.
     * Convenience endpoint (same result as toggle when active).
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExamSummaryResponse>>
    deactivateExam(@PathVariable Long id) {
        return ok("Exam deactivated.",
                examService.deactivateExam(id));
    }

    // ═════════════════════════════════════════════════════════════
    // ADMIN FACING — Question Management (inside exam popup editor)
    // ═════════════════════════════════════════════════════════════

    /**
     * GET /api/exams/{examId}/questions
     * All questions for an exam WITH correct answers + explanations.
     * Used in admin popup edit modal to show existing questions.
     * Supports: Java | Python | C# | SQL | JavaScript | General
     */
    @GetMapping("/{examId}/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>>
    getQuestionsAdmin(@PathVariable Long examId) {
        return ok("Questions fetched (with answers).",
                questionService.getQuestionsForAdmin(examId));
    }

    /**
     * GET /api/exams/{examId}/questions/summaries
     * Lightweight question list — no options, no answers.
     * Used in admin exam overview table for quick scan.
     */
    @GetMapping("/{examId}/questions/summaries")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<QuestionSummaryResponse>>>
    getQuestionSummaries(@PathVariable Long examId) {
        return ok("Question summaries.",
                questionService.getQuestionSummaries(examId));
    }

    /**
     * GET /api/exams/{examId}/questions/user
     * All questions for a specific exam WITHOUT correct answers.
     * Used when student is taking the exam.
     */
    @GetMapping("/{examId}/questions/user")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>>
    getQuestionsForUser(@PathVariable Long examId) {
        return ok("Questions for exam.",
                questionService.getQuestionsForUser(examId));
    }

    /**
     * GET /api/exams/{examId}/questions/language/{language}
     * Questions filtered by programming language.
     * Languages: java | python | csharp | sql | javascript | general
     */
    @GetMapping("/{examId}/questions/language/{language}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>>
    getByLanguage(@PathVariable Long examId,
                  @PathVariable String language) {
        return ok("Questions by language.",
                questionService.getByLanguage(examId, language));
    }

    /**
     * GET /api/exams/{examId}/questions/count
     * Total question count for an exam.
     * Used in admin exam card stat badge.
     */
    @GetMapping("/{examId}/questions/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>>
    getQuestionCount(@PathVariable Long examId) {
        return ok("Question count.",
                questionService.countByExam(examId));
    }

    /**
     * POST /api/exams/{examId}/questions
     * Add a single MCQ question to an exam.
     *
     * Supports:
     *   - Java, Python, C#, SQL, JavaScript questions
     *   - Optional code snippet with language tag
     *   - 2–4 options (A, B, C, D)
     *   - correctOptionIndex (0-based)
     *   - Optional explanation shown post-submission
     *
     * Returns HTTP 201 Created.
     * Action is audit-logged.
     */
    @PostMapping("/{examId}/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestionResponse>>
    addQuestion(@PathVariable Long examId,
                @Valid @RequestBody QuestionRequest req) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Question added.",
                        questionService.addQuestion(examId, req)));
    }

    /**
     * POST /api/exams/{examId}/questions/bulk
     * Add multiple questions at once — used in popup editor
     * when admin pastes or imports a full question set.
     *
     * Body: { "questions": [ {...}, {...} ] }
     * Returns HTTP 201 Created.
     */
    @PostMapping("/{examId}/questions/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>>
    addBulkQuestions(
            @PathVariable Long examId,
            @Valid @RequestBody BulkQuestionRequest req) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Questions added in bulk.",
                        questionService.addBulkQuestions(
                                examId, req)));
    }

    /**
     * PUT /api/exams/{examId}/questions/{questionId}
     * Update a specific question within an exam.
     * Replaces content, code snippet, options, correct answer,
     * and explanation.
     * Action is audit-logged.
     */
    @PutMapping("/{examId}/questions/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestionResponse>>
    updateQuestion(
            @PathVariable Long examId,
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionRequest req) {
        return ok("Question updated.",
                questionService.updateQuestion(
                        questionId, req));
    }

    /**
     * DELETE /api/exams/{examId}/questions/{questionId}
     * Delete a question from an exam.
     * Remaining questions are automatically re-ordered.
     * Action is audit-logged.
     */
    @DeleteMapping("/{examId}/questions/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>>
    deleteQuestion(
            @PathVariable Long examId,
            @PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);
        return ok("Question deleted.", null);
    }

    /**
     * PATCH /api/exams/{examId}/questions/reorder
     * Reorder questions using drag-and-drop in popup editor.
     *
     * Body:
     * {
     *   "orderedQuestionIds": [3, 1, 4, 2]
     * }
     *
     * Each ID gets its position index as the new order (1-based).
     */
    @PatchMapping("/{examId}/questions/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<QuestionSummaryResponse>>>
    reorderQuestions(
            @PathVariable Long examId,
            @Valid @RequestBody ReorderQuestionsRequest req) {
        return ok("Questions reordered.",
                questionService.reorderQuestions(examId, req));
    }

    /**
     * GET /api/exams/questions/admin/{questionId}
     * Get a single question detail including correct answer.
     * Used in admin question edit form.
     */
    @GetMapping("/questions/admin/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestionResponse>>
    getQuestionById(@PathVariable Long questionId) {
        return ok("Question fetched.",
                questionService.getQuestionById(questionId));
    }

    // ═════════════════════════════════════════════════════════════
    // ADMIN FACING — Attempt Results Management
    // ═════════════════════════════════════════════════════════════

    /**
     * POST /api/exams/{examId}/results/{attemptId}/resend-email
     * Admin manually re-dispatches the result email
     * to the student for a specific attempt.
     * Used when automatic email failed on submission.
     */
    @PostMapping("/{examId}/results/{attemptId}/resend-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>>
    resendResultEmail(
            @PathVariable Long examId,
            @PathVariable Long attemptId) {
        attemptService.resendResultEmail(attemptId);
        return ok("Result email re-dispatched.", null);
    }

    // ═════════════════════════════════════════════════════════════
    // CONVENIENCE
    // ═════════════════════════════════════════════════════════════

    private <T> ResponseEntity<ApiResponse<T>> ok(
            String msg, T data) {
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }
}