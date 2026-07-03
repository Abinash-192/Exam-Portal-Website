package com.examportal.controller;

import com.examportal.dto.request.AdminActionRequest;
import com.examportal.dto.request.AdminUpdateUserRequest;
import com.examportal.dto.response.*;
import com.examportal.service.AdminService;
import com.examportal.service.ExamAttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

     private final AdminService adminService;
     private final ExamAttemptService attemptService;

     @GetMapping("/dashboard")
     public  ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard(){

        return ok("Dashboard stats fetched.", adminService.getDashboardStats());
     }

     @GetMapping("/users")
     public ResponseEntity<ApiResponse<List<UserResponse>>>  getAllUsers() {
         return ok("All users fetched.", adminService.getAllUsers());
     }

     @GetMapping("/users/students")
     public ResponseEntity<ApiResponse<List<UserResponse>>>  getAllStudents(){
          return ok("All students fetched.", adminService.getAllStudents());
     }

     @GetMapping("/users/pending")
     public ResponseEntity<ApiResponse<List<UserResponse>>>  getPendingUsers(){
          return ok("Pending approval user fetched.", adminService.getPendingUsers());
     }

     @GetMapping("/users/blocked")
     public ResponseEntity<ApiResponse<List<UserResponse>>>  getBlockedUsers(){
          return ok("Blocked users fetched.", adminService.getBlockedUsers());
     }

     @GetMapping("/users/{id}")
     public ResponseEntity<ApiResponse<UserResponse>>  getUserById(@PathVariable Long id){
          return ok("User fetched.", adminService.getUserById(id));
     }

     @GetMapping("/users/{id}/stats")
     public ResponseEntity<ApiResponse<UserStatsResponse>>  getUserStats(@PathVariable Long id){
          return ok("User stats fetched.", adminService.getUserStats(id));
     }

     @GetMapping("/users/search")
     public ResponseEntity<ApiResponse<List<UserResponse>>>  searchUsers(@RequestParam  String keyword){
          return ok("Search results.", adminService.searchUsers(keyword));
     }

     @PutMapping("/users/{id}/approve")
     public ResponseEntity<ApiResponse<UserResponse>>  approveUser(@PathVariable Long id,
                                                                   @RequestBody(required = false)AdminActionRequest req){
          return ok("User approved successfully.", adminService.approveUser(id, req != null ? req.getReason() : null));
     }

     @PutMapping("/users/{id}/block")
     public ResponseEntity<ApiResponse<UserResponse>>  blockUser(@PathVariable Long id,
                                                                 @RequestBody(required = false) AdminActionRequest req){
          return ok("User blocked.",adminService.blockUser(id, req != null ? req.getReason() : null));
     }

     @PutMapping("/users/{id}/unblock")
     public ResponseEntity<ApiResponse<UserResponse>>  unblockUser(@PathVariable Long id, @RequestBody(required = false) AdminActionRequest req){
          return ok("User unblocked.",adminService.unBlockUser(id, req != null ? req.getReason() : null));
     }

     @PatchMapping("/users/{id}")
     public ResponseEntity<ApiResponse<UserResponse>>  updateUser(@PathVariable Long id, @Valid @RequestBody AdminUpdateUserRequest req){
          return ok("User updated.", adminService.updateUser(id, req));
     }

     @DeleteMapping("/users/{id}")
     public ResponseEntity<ApiResponse<Void>>  deleteUser(@PathVariable Long id, @RequestBody(required = false) AdminActionRequest req){
          adminService.deleteUser(id, req != null ? req.getReason() : null);
          return ok("User deleted Permanetly.", null);
     }

     @GetMapping("/results/exam/{examId}")
     public ResponseEntity<ApiResponse<List<AttemptResultResponse>>>  getExamResults(@PathVariable Long examId){
          return  ok("Exam results fetched.", adminService.getExamResults(examId));
     }

     @GetMapping("/results/user/{userId}")
     public ResponseEntity<ApiResponse<List<AttemptResultResponse>>>  getUserResults(@PathVariable Long userId){
          return ok("User results fetched.", adminService.getUserResults(userId));
     }

     @PostMapping("/results/{attemptId}/resend-email")
     public ResponseEntity<ApiResponse<Void>>  resendResultEmail(@PathVariable Long attemptId){
          attemptService.resendResultEmail(attemptId);
          return ok("Result email re-dispatched.",null);
     }

     @GetMapping("/activity")
     public ResponseEntity<ApiResponse<List<AdminActivityResponse>>>  getRecentActivity(){
          return ok("Recent activity fetched.", adminService.getRecentActivity());
     }

     @GetMapping("/activity/admin/{adminId}")
     public ResponseEntity<ApiResponse<List<AdminActivityResponse>>>  getActivityByAdmin(@PathVariable Long adminId){
          return ok("Admin activity fetched.", adminService.getActivityByAdmin(adminId));
     }

     @GetMapping("/activity/range")
     public ResponseEntity<ApiResponse<List<AdminActivityResponse>>>  getActivityByRange(@RequestParam
                                                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                                         LocalDateTime from,
                                                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                                         LocalDateTime to){
           return ok("Activity by date range.", adminService.getActivityByDateRange(from, to));
     }
     private <T> ResponseEntity<ApiResponse<T>> ok(String msg , T data){
          return ResponseEntity.ok(ApiResponse.success(msg, data));
     }
}
