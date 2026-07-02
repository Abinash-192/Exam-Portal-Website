package com.examportal.controller;

import com.examportal.dto.request.AdminActionRequest;
import com.examportal.dto.response.AdminDashboardResponse;
import com.examportal.dto.response.ApiResponse;
import com.examportal.dto.response.UserResponse;
import com.examportal.dto.response.UserStatsResponse;
import com.examportal.service.AdminService;
import com.examportal.service.ExamAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;

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
          return ok("User approved successfully.", adminService.approveUser(id, req != null ? req.getReason() : null))
     }
     private <T> ResponseEntity<ApiResponse<T>> ok(String msg , T data){
          return ResponseEntity.ok(ApiResponse.success(msg, data));
     }
}
