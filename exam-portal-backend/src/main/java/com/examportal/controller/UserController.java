package com.examportal.controller;

import com.examportal.dto.request.ChangePasswordRequest;
import com.examportal.dto.request.UpdateProfileRequest;
import com.examportal.dto.response.*;
import com.examportal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ═════════════════════════════════════════════════════════════
    // DASHBOARD
    // ═════════════════════════════════════════════════════════════

    // GET /api/user/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<UserDashboardResponse>>
    getDashboard(
            @AuthenticationPrincipal UserDetails principal) {
        return ok("Dashboard fetched.",
                userService.getDashboard(
                        principal.getUsername()));
    }

    // ═════════════════════════════════════════════════════════════
    // PROFILE
    // ═════════════════════════════════════════════════════════════

    // GET /api/user/profile
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>>
    getProfile(
            @AuthenticationPrincipal UserDetails principal) {
        return ok("Profile fetched.",
                userService.getProfile(
                        principal.getUsername()));
    }

    // PATCH /api/user/profile
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>>
    updateProfile(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateProfileRequest req) {
        return ok("Profile updated.",
                userService.updateProfile(
                        principal.getUsername(), req));
    }

    // ═════════════════════════════════════════════════════════════
    // PASSWORD
    // ═════════════════════════════════════════════════════════════

    // POST /api/user/change-password
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>>
    changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ChangePasswordRequest req) {
        return ok("Password changed.",
                userService.changePassword(
                        principal.getUsername(), req));
    }

    // ═════════════════════════════════════════════════════════════
    // CONVENIENCE
    // ═════════════════════════════════════════════════════════════

    private <T> ResponseEntity<ApiResponse<T>> ok(
            String msg, T data) {
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }
}