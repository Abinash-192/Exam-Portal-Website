package com.examportal.controller;

import com.examportal.dto.response.ApiResponse;
import com.examportal.dto.response.UserResponse;
import com.examportal.repository.UserRepository;
import com.examportal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@AuthenticationPrincipal UserDetails principal){

        return  ResponseEntity.ok(ApiResponse.success(
                "Profile fetched.", userService.getByEmail(principal.getUsername())
        ));
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<ApiResponse<UserResponse>> getDashboardStats(@AuthenticationPrincipal UserDetails pricipal){

        return ResponseEntity.ok(ApiResponse.success(
                "Stats Fetched.",userService.getByEmail(pricipal.getUsername())
        ));
    }

}
