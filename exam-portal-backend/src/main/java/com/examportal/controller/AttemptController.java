package com.examportal.controller;

import com.examportal.dto.request.SaveAnswerRequest;
import com.examportal.dto.request.StartAttemptRequest;
import com.examportal.dto.request.SubmitAttemptRequest;
import com.examportal.dto.response.ApiResponse;
import com.examportal.dto.response.AttemptResultResponse;
import com.examportal.dto.response.AttemptStartResponse;
import com.examportal.dto.response.TimerStatusResponse;
import com.examportal.model.User;
import com.examportal.repository.UserRepository;
import com.examportal.service.ExamAttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attempts")
@RequiredArgsConstructor
public class AttemptController {

    private final ExamAttemptService attemptService;
    private final UserRepository     userRepository;

    // ═════════════════════════════════════════════════════════════
    // START EXAM — initialise timer
    // ═════════════════════════════════════════════════════════════

    // POST /api/attempts/start
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<AttemptStartResponse>>
    startExam(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody StartAttemptRequest req) {
        return ok("Exam started. Timer is running.",
                attemptService.startAttempt(
                        resolve(principal).getId(), req));
    }

    // ═════════════════════════════════════════════════════════════
    // SAVE ANSWER (auto-save per question click)
    // ═════════════════════════════════════════════════════════════

    // POST /api/attempts/save-answer
    @PostMapping("/save-answer")
    public ResponseEntity<ApiResponse<String>>
    saveAnswer(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody SaveAnswerRequest req) {
        return ok("Answer saved.",
                attemptService.saveAnswer(
                        resolve(principal).getId(), req));
    }

    // ═════════════════════════════════════════════════════════════
    // TIMER STATUS (poll every 30 sec)
    // ═════════════════════════════════════════════════════════════

    // GET /api/attempts/{attemptId}/timer
    @GetMapping("/{attemptId}/timer")
    public ResponseEntity<ApiResponse<TimerStatusResponse>>
    getTimerStatus(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long attemptId) {
        return ok("Timer status.",
                attemptService.getTimerStatus(
                        resolve(principal).getId(), attemptId));
    }

    // ═════════════════════════════════════════════════════════════
    // SUBMIT EXAM (manual)
    // ═════════════════════════════════════════════════════════════

    // POST /api/attempts/submit
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<AttemptResultResponse>>
    submitExam(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody SubmitAttemptRequest req) {
        return ok("Exam submitted successfully.",
                attemptService.submitAttempt(
                        resolve(principal).getId(), req));
    }

    // ═════════════════════════════════════════════════════════════
    // MY RESULTS
    // ═════════════════════════════════════════════════════════════

    // GET /api/attempts/my-results
    @GetMapping("/my-results")
    public ResponseEntity<ApiResponse<List<AttemptResultResponse>>>
    myResults(
            @AuthenticationPrincipal UserDetails principal) {
        return ok("Your results.",
                attemptService.getUserResults(
                        resolve(principal).getId()));
    }

    // ═════════════════════════════════════════════════════════════
    // ADMIN ENDPOINTS
    // ═════════════════════════════════════════════════════════════

    // GET /api/attempts/exam/{examId}/results
    @GetMapping("/exam/{examId}/results")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AttemptResultResponse>>>
    examResults(@PathVariable Long examId) {
        return ok("Exam results.",
                attemptService.getExamResults(examId));
    }

    // POST /api/attempts/{attemptId}/resend-email
    @PostMapping("/{attemptId}/resend-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>>
    resendEmail(@PathVariable Long attemptId) {
        attemptService.resendResultEmail(attemptId);
        return ok("Result email re-dispatched.", null);
    }

    // ═════════════════════════════════════════════════════════════
    // CONVENIENCE
    // ═════════════════════════════════════════════════════════════

    private User resolve(UserDetails principal) {
        return userRepository
                .findByEmail(principal.getUsername())
                .orElseThrow();
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(
            String msg, T data) {
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }
}