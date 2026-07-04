//package com.examportal.controller;
//
//import com.examportal.dto.request.AdminActionRequest;
//import com.examportal.dto.request.AdminUpdateUserRequest;
//import com.examportal.dto.response.*;
//import com.examportal.service.AdminService;
//import com.examportal.service.ExamAttemptService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.http.ResponseEntity;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@RestController
//@RequestMapping("api/admin")
//@PreAuthorize("hasRole('ADMIN')")
//@RequiredArgsConstructor
//public class AdminController {
//
//     private final AdminService adminService;
//     private final ExamAttemptService attemptService;
//
//     @GetMapping("/dashboard")
//     public  ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard(){
//
//        return ok("Dashboard stats fetched.", adminService.getDashboardStats());
//     }
//
//     @GetMapping("/users")
//     public ResponseEntity<ApiResponse<List<UserResponse>>>  getAllUsers() {
//         return ok("All users fetched.", adminService.getAllUsers());
//     }
//
//     @GetMapping("/users/students")
//     public ResponseEntity<ApiResponse<List<UserResponse>>>  getAllStudents(){
//          return ok("All students fetched.", adminService.getAllStudents());
//     }
//
//     @GetMapping("/users/pending")
//     public ResponseEntity<ApiResponse<List<UserResponse>>>  getPendingUsers(){
//          return ok("Pending approval user fetched.", adminService.getPendingUsers());
//     }
//
//     @GetMapping("/users/blocked")
//     public ResponseEntity<ApiResponse<List<UserResponse>>>  getBlockedUsers(){
//          return ok("Blocked users fetched.", adminService.getBlockedUsers());
//     }
//
//     @GetMapping("/users/{id}")
//     public ResponseEntity<ApiResponse<UserResponse>>  getUserById(@PathVariable Long id){
//          return ok("User fetched.", adminService.getUserById(id));
//     }
//
//     @GetMapping("/users/{id}/stats")
//     public ResponseEntity<ApiResponse<UserStatsResponse>>  getUserStats(@PathVariable Long id){
//          return ok("User stats fetched.", adminService.getUserStats(id));
//     }
//
//     @GetMapping("/users/search")
//     public ResponseEntity<ApiResponse<List<UserResponse>>>  searchUsers(@RequestParam  String keyword){
//          return ok("Search results.", adminService.searchUsers(keyword));
//     }
//
//     @PutMapping("/users/{id}/approve")
//     public ResponseEntity<ApiResponse<UserResponse>>  approveUser(@PathVariable Long id,
//                                                                   @RequestBody(required = false)AdminActionRequest req){
//          return ok("User approved successfully.", adminService.approveUser(id, req != null ? req.getReason() : null));
//     }
//
//     @PutMapping("/users/{id}/block")
//     public ResponseEntity<ApiResponse<UserResponse>>  blockUser(@PathVariable Long id,
//                                                                 @RequestBody(required = false) AdminActionRequest req){
//          return ok("User blocked.",adminService.blockUser(id, req != null ? req.getReason() : null));
//     }
//
//     @PutMapping("/users/{id}/unblock")
//     public ResponseEntity<ApiResponse<UserResponse>>  unblockUser(@PathVariable Long id, @RequestBody(required = false) AdminActionRequest req){
//          return ok("User unblocked.",adminService.unBlockUser(id, req != null ? req.getReason() : null));
//     }
//
//     @PatchMapping("/users/{id}")
//     public ResponseEntity<ApiResponse<UserResponse>>  updateUser(@PathVariable Long id, @Valid @RequestBody AdminUpdateUserRequest req){
//          return ok("User updated.", adminService.updateUser(id, req));
//     }
//
//     @DeleteMapping("/users/{id}")
//     public ResponseEntity<ApiResponse<Void>>  deleteUser(@PathVariable Long id, @RequestBody(required = false) AdminActionRequest req){
//          adminService.deleteUser(id, req != null ? req.getReason() : null);
//          return ok("User deleted Permanetly.", null);
//     }
//
//     @GetMapping("/results/exam/{examId}")
//     public ResponseEntity<ApiResponse<List<AttemptResultResponse>>>  getExamResults(@PathVariable Long examId){
//          return  ok("Exam results fetched.", adminService.getExamResults(examId));
//     }
//
//     @GetMapping("/results/user/{userId}")
//     public ResponseEntity<ApiResponse<List<AttemptResultResponse>>>  getUserResults(@PathVariable Long userId){
//          return ok("User results fetched.", adminService.getUserResults(userId));
//     }
//
//     @PostMapping("/results/{attemptId}/resend-email")
//     public ResponseEntity<ApiResponse<Void>>  resendResultEmail(@PathVariable Long attemptId){
//          attemptService.resendResultEmail(attemptId);
//          return ok("Result email re-dispatched.",null);
//     }
//
//     @GetMapping("/activity")
//     public ResponseEntity<ApiResponse<List<AdminActivityResponse>>>  getRecentActivity(){
//          return ok("Recent activity fetched.", adminService.getRecentActivity());
//     }
//
//     @GetMapping("/activity/admin/{adminId}")
//     public ResponseEntity<ApiResponse<List<AdminActivityResponse>>>  getActivityByAdmin(@PathVariable Long adminId){
//          return ok("Admin activity fetched.", adminService.getActivityByAdmin(adminId));
//     }
//
//     @GetMapping("/activity/range")
//     public ResponseEntity<ApiResponse<List<AdminActivityResponse>>>  getActivityByRange(@RequestParam
//                                                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//                                                                                         LocalDateTime from,
//                                                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//                                                                                         LocalDateTime to){
//           return ok("Activity by date range.", adminService.getActivityByDateRange(from, to));
//     }
//     private <T> ResponseEntity<ApiResponse<T>> ok(String msg , T data){
//          return ResponseEntity.ok(ApiResponse.success(msg, data));
//     }
//}



package com.examportal.controller;

import com.examportal.dto.request.*;
import com.examportal.dto.response.*;
import com.examportal.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

     private final AdminService       adminService;
     private final ExamService        examService;
     private final QuestionService    questionService;
     private final ExamAttemptService attemptService;

     // ═════════════════════════════════════════════════════════════
     // DASHBOARD
     // ═════════════════════════════════════════════════════════════

     @GetMapping("/dashboard")
     public ResponseEntity<ApiResponse<AdminDashboardResponse>>
     getDashboard() {
          return ok("Dashboard stats fetched.",
                  adminService.getDashboardStats());
     }

     // ═════════════════════════════════════════════════════════════
     // USER LISTING
     // ═════════════════════════════════════════════════════════════

     // GET /api/admin/users — all users
     @GetMapping("/users")
     public ResponseEntity<ApiResponse<List<UserResponse>>>
     getAllUsers() {
          return ok("All users fetched.",
                  adminService.getAllUsers());
     }

     // GET /api/admin/users/students — students only
     @GetMapping("/users/students")
     public ResponseEntity<ApiResponse<List<UserResponse>>>
     getAllStudents() {
          return ok("All students fetched.",
                  adminService.getAllStudents());
     }

     // GET /api/admin/users/pending — awaiting approval
     @GetMapping("/users/pending")
     public ResponseEntity<ApiResponse<List<UserResponse>>>
     getPendingUsers() {
          return ok("Pending approval users fetched.",
                  adminService.getPendingUsers());
     }

     // GET /api/admin/users/blocked
     @GetMapping("/users/blocked")
     public ResponseEntity<ApiResponse<List<UserResponse>>>
     getBlockedUsers() {
          return ok("Blocked users fetched.",
                  adminService.getBlockedUsers());
     }

     // GET /api/admin/users/{id}
     @GetMapping("/users/{id}")
     public ResponseEntity<ApiResponse<UserResponse>>
     getUserById(@PathVariable Long id) {
          return ok("User fetched.",
                  adminService.getUserById(id));
     }

     // GET /api/admin/users/{id}/stats
     @GetMapping("/users/{id}/stats")
     public ResponseEntity<ApiResponse<UserStatsResponse>>
     getUserStats(@PathVariable Long id) {
          return ok("User stats fetched.",
                  adminService.getUserStats(id));
     }

     // GET /api/admin/users/search?keyword=john
     @GetMapping("/users/search")
     public ResponseEntity<ApiResponse<List<UserResponse>>>
     searchUsers(@RequestParam String keyword) {
          return ok("Search results.",
                  adminService.searchUsers(keyword));
     }

     // ═════════════════════════════════════════════════════════════
     // USER ACTIONS
     // ═════════════════════════════════════════════════════════════

     // PUT /api/admin/users/{id}/approve
     @PutMapping("/users/{id}/approve")
     public ResponseEntity<ApiResponse<UserResponse>>
     approveUser(@PathVariable Long id,
                 @RequestBody(required = false)
                 AdminActionRequest req) {
          return ok("User approved. They can now take exams.",
                  adminService.approveUser(id,
                          req != null ? req.getReason() : null));
     }

     // PUT /api/admin/users/{id}/block
     @PutMapping("/users/{id}/block")
     public ResponseEntity<ApiResponse<UserResponse>>
     blockUser(@PathVariable Long id,
               @RequestBody(required = false)
               AdminActionRequest req) {
          return ok("User blocked. They cannot access the portal.",
                  adminService.blockUser(id,
                          req != null ? req.getReason() : null));
     }

     // PUT /api/admin/users/{id}/unblock
     @PutMapping("/users/{id}/unblock")
     public ResponseEntity<ApiResponse<UserResponse>>
     unblockUser(@PathVariable Long id,
                 @RequestBody(required = false)
                 AdminActionRequest req) {
          return ok("User unblocked. They can access the portal again.",
                  adminService.unblockUser(id,
                          req != null ? req.getReason() : null));
     }

     // PATCH /api/admin/users/{id}
     @PatchMapping("/users/{id}")
     public ResponseEntity<ApiResponse<UserResponse>>
     updateUser(@PathVariable Long id,
                @Valid @RequestBody AdminUpdateUserRequest req) {
          return ok("User profile updated.",
                  adminService.updateUser(id, req));
     }

     // DELETE /api/admin/users/{id}
     @DeleteMapping("/users/{id}")
     public ResponseEntity<ApiResponse<Void>>
     deleteUser(@PathVariable Long id,
                @RequestBody(required = false)
                AdminActionRequest req) {
          adminService.deleteUser(id,
                  req != null ? req.getReason() : null);
          return ok("User permanently deleted.", null);
     }

     // ═════════════════════════════════════════════════════════════
     // EXAM MANAGEMENT
     // ═════════════════════════════════════════════════════════════

     // GET /api/admin/exams — all exams including inactive
     @GetMapping("/exams")
     public ResponseEntity<ApiResponse<List<ExamResponse>>>
     getAllExams() {
          return ok("All exams fetched.",
                  examService.getAllExamsAdmin());
     }

     // GET /api/admin/exams/categories
     @GetMapping("/exams/categories")
     public ResponseEntity<ApiResponse<List<String>>>
     getAllCategories() {
          return ok("All exam categories.",
                  examService.getAllCategories());
     }

     // GET /api/admin/exams/{id}
     @GetMapping("/exams/{id}")
     public ResponseEntity<ApiResponse<ExamResponse>>
     getExamAdmin(@PathVariable Long id) {
          return ok("Exam detail fetched.",
                  examService.getExamForAdmin(id));
     }

     // GET /api/admin/exams/{id}/stats
     @GetMapping("/exams/{id}/stats")
     public ResponseEntity<ApiResponse<ExamStatsResponse>>
     getExamStats(@PathVariable Long id) {
          return ok("Exam analytics fetched.",
                  examService.getExamStats(id));
     }

     // POST /api/admin/exams — create exam
     @PostMapping("/exams")
     public ResponseEntity<ApiResponse<ExamResponse>>
     createExam(@Valid @RequestBody ExamRequest req) {
          return ResponseEntity
                  .status(HttpStatus.CREATED)
                  .body(ApiResponse.success(
                          "Exam created successfully.",
                          examService.createExam(req)));
     }

     // PUT /api/admin/exams/{id} — popup edit
     @PutMapping("/exams/{id}")
     public ResponseEntity<ApiResponse<ExamResponse>>
     updateExam(@PathVariable Long id,
                @Valid @RequestBody ExamRequest req) {
          return ok("Exam updated successfully.",
                  examService.updateExam(id, req));
     }

     // DELETE /api/admin/exams/{id}
     @DeleteMapping("/exams/{id}")
     public ResponseEntity<ApiResponse<Void>>
     deleteExam(@PathVariable Long id) {
          examService.deleteExam(id);
          return ok("Exam permanently deleted.", null);
     }

     // PATCH /api/admin/exams/{id}/toggle
     @PatchMapping("/exams/{id}/toggle")
     public ResponseEntity<ApiResponse<ExamSummaryResponse>>
     toggleExamStatus(@PathVariable Long id) {
          return ok("Exam status toggled.",
                  examService.toggleStatus(id));
     }

     // ═════════════════════════════════════════════════════════════
     // QUESTION MANAGEMENT (inside exam popup editor)
     // ═════════════════════════════════════════════════════════════

     // GET /api/admin/exams/{examId}/questions
     @GetMapping("/exams/{examId}/questions")
     public ResponseEntity<ApiResponse<List<QuestionResponse>>>
     getQuestionsAdmin(@PathVariable Long examId) {
          return ok("Questions fetched (with answers).",
                  questionService.getQuestionsForAdmin(examId));
     }

     // GET /api/admin/exams/{examId}/questions/summaries
     @GetMapping("/exams/{examId}/questions/summaries")
     public ResponseEntity<ApiResponse<List<QuestionSummaryResponse>>>
     getQuestionSummaries(@PathVariable Long examId) {
          return ok("Question summaries.",
                  questionService.getQuestionSummaries(examId));
     }

     // GET /api/admin/questions/{questionId}
     @GetMapping("/questions/{questionId}")
     public ResponseEntity<ApiResponse<QuestionResponse>>
     getQuestionById(@PathVariable Long questionId) {
          return ok("Question fetched.",
                  questionService.getQuestionById(questionId));
     }

     // POST /api/admin/exams/{examId}/questions
     @PostMapping("/exams/{examId}/questions")
     public ResponseEntity<ApiResponse<QuestionResponse>>
     addQuestion(@PathVariable Long examId,
                 @Valid @RequestBody QuestionRequest req) {
          return ResponseEntity
                  .status(HttpStatus.CREATED)
                  .body(ApiResponse.success(
                          "Question added.",
                          questionService.addQuestion(examId, req)));
     }

     // POST /api/admin/exams/{examId}/questions/bulk
     @PostMapping("/exams/{examId}/questions/bulk")
     public ResponseEntity<ApiResponse<List<QuestionResponse>>>
     addBulkQuestions(@PathVariable Long examId,
                      @Valid @RequestBody BulkQuestionRequest req) {
          return ResponseEntity
                  .status(HttpStatus.CREATED)
                  .body(ApiResponse.success(
                          "Questions added in bulk.",
                          questionService.addBulkQuestions(examId, req)));
     }

     // PUT /api/admin/questions/{questionId}
     @PutMapping("/questions/{questionId}")
     public ResponseEntity<ApiResponse<QuestionResponse>>
     updateQuestion(@PathVariable Long questionId,
                    @Valid @RequestBody QuestionRequest req) {
          return ok("Question updated.",
                  questionService.updateQuestion(questionId, req));
     }

     // DELETE /api/admin/questions/{questionId}
     @DeleteMapping("/questions/{questionId}")
     public ResponseEntity<ApiResponse<Void>>
     deleteQuestion(@PathVariable Long questionId) {
          questionService.deleteQuestion(questionId);
          return ok("Question deleted.", null);
     }

     // PATCH /api/admin/exams/{examId}/questions/reorder
     @PatchMapping("/exams/{examId}/questions/reorder")
     public ResponseEntity<ApiResponse<List<QuestionSummaryResponse>>>
     reorderQuestions(@PathVariable Long examId,
                      @Valid @RequestBody ReorderQuestionsRequest req) {
          return ok("Questions reordered.",
                  questionService.reorderQuestions(examId, req));
     }

     // ═════════════════════════════════════════════════════════════
     // RESULTS MANAGEMENT
     // ═════════════════════════════════════════════════════════════

     // GET /api/admin/results/exam/{examId}
     @GetMapping("/results/exam/{examId}")
     public ResponseEntity<ApiResponse<List<AttemptResultResponse>>>
     getExamResults(@PathVariable Long examId) {
          return ok("Exam results fetched.",
                  adminService.getExamResults(examId));
     }

     // GET /api/admin/results/user/{userId}
     @GetMapping("/results/user/{userId}")
     public ResponseEntity<ApiResponse<List<AttemptResultResponse>>>
     getUserResults(@PathVariable Long userId) {
          return ok("User results fetched.",
                  adminService.getUserResults(userId));
     }

     // POST /api/admin/results/{attemptId}/resend-email
     @PostMapping("/results/{attemptId}/resend-email")
     public ResponseEntity<ApiResponse<Void>>
     resendResultEmail(@PathVariable Long attemptId) {
          attemptService.resendResultEmail(attemptId);
          return ok("Result email re-dispatched.", null);
     }

     // ═════════════════════════════════════════════════════════════
     // AUDIT LOG
     // ═════════════════════════════════════════════════════════════

     // GET /api/admin/activity
     @GetMapping("/activity")
     public ResponseEntity<ApiResponse<List<AdminActivityResponse>>>
     getRecentActivity() {
          return ok("Recent activity fetched.",
                  adminService.getRecentActivity());
     }

     // GET /api/admin/activity/admin/{adminId}
     @GetMapping("/activity/admin/{adminId}")
     public ResponseEntity<ApiResponse<List<AdminActivityResponse>>>
     getActivityByAdmin(@PathVariable Long adminId) {
          return ok("Admin activity fetched.",
                  adminService.getActivityByAdmin(adminId));
     }

     // GET /api/admin/activity/range?from=...&to=...
     @GetMapping("/activity/range")
     public ResponseEntity<ApiResponse<List<AdminActivityResponse>>>
     getActivityByRange(
             @RequestParam
             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
             LocalDateTime from,
             @RequestParam
             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
             LocalDateTime to) {
          return ok("Activity by date range.",
                  adminService.getActivityByDateRange(from, to));
     }

     // ═════════════════════════════════════════════════════════════
     // CONVENIENCE
     // ═════════════════════════════════════════════════════════════

     private <T> ResponseEntity<ApiResponse<T>> ok(String msg, T data) {
          return ResponseEntity.ok(ApiResponse.success(msg, data));
     }
}