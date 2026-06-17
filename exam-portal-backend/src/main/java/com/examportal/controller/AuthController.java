package com.examportal.controller;


import com.examportal.dto.request.LoginRequest;
import com.examportal.dto.request.OtpVerifyRequest;
import com.examportal.dto.request.RegisterRequest;
import com.examportal.dto.response.ApiResponse;
import com.examportal.dto.response.AuthResponse;
import com.examportal.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest req){

        return  ResponseEntity.ok(ApiResponse.success(authService.register(req), null));
    }

    //POST/api/auth/verify-otp
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody OtpVerifyRequest req){

        String msg = authService.verifyEmailOtp(req.getIdentifier(),req.getOtp());
        return ResponseEntity.ok(ApiResponse.success(msg,null));
    }

    //POST/ api/auth/resend-otp ? email
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendOtp(@RequestParam String email){

        return ResponseEntity.ok(ApiResponse.success(authService.resendOtp(email), null));
    }

    //Post/api/auth/login
    @PostMapping("/login")
    public  ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req){

        return  ResponseEntity.ok(ApiResponse.success("Login Successful.", authService.login(req)));
    }

    //Post/api//auth/refresh-token ? token
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestParam String token){

        return ResponseEntity.ok(ApiResponse.success("Token refreshed.", authService.refreshToken(token)));
    }

    //Post/api/auth/forgot-password
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestParam String email){

        return ResponseEntity.ok(ApiResponse.success(authService.forgotPassword(email), null));
    }

    //Post/api/auth/reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestParam String email,
                                                             @RequestParam String otp,
                                                             @RequestParam String newPassword){

        return  ResponseEntity.ok(ApiResponse.success(authService.resetPassword(email, otp, newPassword)));
    }
}
