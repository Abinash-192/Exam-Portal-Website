//package com.examportal.service;
//
//import com.examportal.dto.response.UserResponse;
//import com.examportal.exception.ResourceNotFoundException;
//import com.examportal.model.Role;
//import org.springframework.transaction.annotation.Transactional;
//import com.examportal.model.User;
//import com.examportal.repository.ExamAttemptRepository;
//import com.examportal.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//
//@RequiredArgsConstructor
//@Service
//public class UserService {
//
//    private final UserRepository userRepository;
//    private final ExamAttemptRepository attemptRepository;
//
//    public UserResponse getById(Long id){
//
//        return  mapToResponse(findOrThrow(id));
//    }
//
//    public UserResponse getByEmail(String email){
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResourceNotFoundException("No user with email: " +email));
//        return mapToResponse(user);
//    }
//
//    public List<UserResponse> getAllUsers(){
//
//        return userRepository.findAll().stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    public List<UserResponse> getPendingUsers(){
//
//        return userRepository.findByEmailVerifiedTrueAndApprovedFalseAndBlockedFalse()
//                .stream().map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    public List<UserResponse> getBlockedUsers(){
//        return userRepository.findByBlocked(true).stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public UserResponse approveUser(Long id){
//
//        User user = findOrThrow(id);
//        user.setApproved(true);
//        user.setEnabled(true);
//        return mapToResponse(userRepository.save(user));
//    }
//
//    @Transactional
//    public UserResponse blockUser(Long id){
//
//        User user = findOrThrow(id);
//        if (user.getRole() == Role.ADMIN) {
//            throw new com.examportal.exception.ValidationException(
//                    "Admin accounts cannot be blocked."
//            );
//
//        }
//        user.setBlocked(true);
//        user.setEnabled(false);
//        return mapToResponse(userRepository.save(user));
//    }
//
//    @Transactional
//    public UserResponse unblockUser(Long id){
//
//        User user = findOrThrow(id);
//        user.setBlocked(false);
//        user.setEnabled(true);
//        return  mapToResponse(userRepository.save(user));
//    }
//
//    @Transactional
//    public void deleteUser(Long id){
//
//        User user = findOrThrow(id);
//        if (user.getRole() == Role.ADMIN) {
//            throw new com.examportal.exception.ValidationException(
//
//                    "Admin accounts cannot be deleted."
//            );
//        }
//        userRepository.delete(user);
//    }
//
//    public long countUsers(){
//
//        return userRepository.countByRole(Role.USER);
//    }
//
//    public long countPendingUsers(){
//        return userRepository.countPendingApprovals();
//    }
//
//    public UserResponse mapToResponse(User user){
//
//        int totalAttempts = attemptRepository.countByUserId(user.getId());
//        int passedAttempts = attemptRepository.countPassedUserId(user.getId());
//
//        return UserResponse.builder()
//                .id(user.getId())
//                .name(user.getName())
//                .email(user.getEmail())
//                .mobile(user.getMobile())
//                .role(user.getRole().name())
//                .profilePicture(user.getProfilePicture())
//                .provider(user.getProvider())
//                .approved(user.isApproved())
//                .blocked(user.isBlocked())
//                .enabled(user.isEnabled())
//                .emailVerified(user.isEmailVerified())
//                .mobileVerified(user.isMobileVerified())
//                .createdAt(user.getCreatedAt())
//                .updatedAt(user.getUpdatedAt())
//                .totalExamsTaken(totalAttempts)
//                .totalExamsPassed(passedAttempts)
//                .build();
//    }
//
//    private User findOrThrow(Long id){
//
//        return userRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found with id : "+id));
//    }
//}


package com.examportal.service;

import com.examportal.dto.request.ChangePasswordRequest;
import com.examportal.dto.request.UpdateProfileRequest;
import com.examportal.dto.response.*;
import com.examportal.exception.ResourceNotFoundException;
import com.examportal.exception.ValidationException;
import com.examportal.model.Role;
import com.examportal.model.User;
import com.examportal.repository.ExamAttemptRepository;
import com.examportal.repository.ExamRepository;
import com.examportal.repository.NotificationRepository;
import com.examportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository         userRepository;
    private final ExamAttemptRepository  attemptRepository;
    private final ExamRepository         examRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder        passwordEncoder;

    // ─────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return mapToResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No user found with email: " + email));
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));

        int taken  = attemptRepository.countByUserId(user.getId());
        int passed = attemptRepository.countPassedByUserId(user.getId());
        Double avg = attemptRepository.avgPercentageByUserId(user.getId());

        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole().name())
                .provider(user.getProvider())
                .profilePicture(user.getProfilePicture())
                .bio(user.getBio())
                .emailVerified(user.isEmailVerified())
                .mobileVerified(user.isMobileVerified())
                .approved(user.isApproved())
                .blocked(user.isBlocked())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .totalExamsTaken(taken)
                .totalExamsPassed(passed)
                .averagePercentage(avg != null
                        ? Math.round(avg * 10.0) / 10.0 : 0.0)
                .performanceBand(computePerformanceBand(
                        avg != null ? avg : 0.0))
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // DASHBOARD
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserDashboardResponse getDashboard(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));

        int taken  = attemptRepository.countByUserId(user.getId());
        int passed = attemptRepository.countPassedByUserId(user.getId());
        int failed = taken - passed;

        Double avgScore = attemptRepository.avgScoreByUserId(user.getId());
        Double avgPct   = attemptRepository.avgPercentageByUserId(user.getId());
        long   available = examRepository.countActiveExams();
        long   unread   = notificationRepository
                .countByUserIdAndReadFalse(user.getId());

        // Recent 5 results
        List<AttemptResultResponse> recent = attemptRepository
                .findByUserIdOrderByAttemptedAtDesc(user.getId())
                .stream()
                .limit(5)
                .map(a -> AttemptResultResponse.builder()
                        .attemptId(a.getId())
                        .examTitle(a.getExam().getTitle())
                        .category(a.getExam().getCategory())
                        .scoreObtained(a.getScoreObtained())
                        .totalMarks(a.getTotalMarks())
                        .percentage(a.getPercentage())
                        .passed(a.isPassed())
                        .performanceBand(
                                computePerformanceBand(a.getPercentage()))
                        .attemptedAt(a.getAttemptedAt())
                        .build())
                .collect(Collectors.toList());

        return UserDashboardResponse.builder()
                .profile(mapToResponse(user))
                .totalExamsTaken(taken)
                .totalExamsPassed(passed)
                .totalExamsFailed(failed)
                .averageScore(avgScore != null
                        ? Math.round(avgScore * 10.0) / 10.0 : 0.0)
                .averagePercentage(avgPct != null
                        ? Math.round(avgPct * 10.0) / 10.0 : 0.0)
                .performanceBand(computePerformanceBand(
                        avgPct != null ? avgPct : 0.0))
                .availableExams(available)
                .recentResults(recent)
                .unreadNotifications(unread)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE PROFILE
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public UserResponse updateProfile(String email,
                                      UpdateProfileRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));

        if (req.getName() != null && !req.getName().isBlank())
            user.setName(req.getName());

        if (req.getMobile() != null && !req.getMobile().isBlank()) {
            // Check mobile uniqueness if it changed
            if (!req.getMobile().equals(user.getMobile())
                    && userRepository.existsByMobile(req.getMobile())) {
                throw new ValidationException(
                        "Mobile number is already registered.");
            }
            user.setMobile(req.getMobile());
        }

        if (req.getBio() != null)
            user.setBio(req.getBio());

        if (req.getProfilePicture() != null)
            user.setProfilePicture(req.getProfilePicture());

        userRepository.save(user);
        log.info("Profile updated for user [{}]", email);
        return mapToResponse(user);
    }

    // ─────────────────────────────────────────────────────────────
    // CHANGE PASSWORD
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public String changePassword(String email,
                                 ChangePasswordRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));

        // OAuth2 users have no local password
        if (user.getPassword() == null)
            throw new ValidationException(
                    "Password cannot be changed for " +
                            user.getProvider() + " accounts.");

        if (!passwordEncoder.matches(
                req.getCurrentPassword(), user.getPassword()))
            throw new ValidationException(
                    "Current password is incorrect.");

        if (!req.getNewPassword().equals(req.getConfirmPassword()))
            throw new ValidationException(
                    "New password and confirm password do not match.");

        if (passwordEncoder.matches(
                req.getNewPassword(), user.getPassword()))
            throw new ValidationException(
                    "New password must be different from the current password.");

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user [{}]", email);
        return "Password updated successfully.";
    }

    // ─────────────────────────────────────────────────────────────
    // LAST LOGIN TIMESTAMP
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public void updateLastLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    // ─────────────────────────────────────────────────────────────
    // ADMIN-FACING HELPERS (called by AdminService)
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getPendingUsers() {
        return userRepository
                .findByEmailVerifiedTrueAndApprovedFalseAndBlockedFalse()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getBlockedUsers() {
        return userRepository.findByBlocked(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse approveUser(Long id) {
        User user = findOrThrow(id);
        user.setApproved(true);
        user.setEnabled(true);
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse blockUser(Long id) {
        User user = findOrThrow(id);
        if (user.getRole() == Role.ADMIN)
            throw new ValidationException(
                    "Admin accounts cannot be blocked.");
        user.setBlocked(true);
        user.setEnabled(false);
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse unblockUser(Long id) {
        User user = findOrThrow(id);
        user.setBlocked(false);
        user.setEnabled(true);
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findOrThrow(id);
        if (user.getRole() == Role.ADMIN)
            throw new ValidationException(
                    "Admin accounts cannot be deleted.");
        userRepository.delete(user);
    }

    // ── Count helpers ─────────────────────────────────────────────
    public long countUsers()            {
        return userRepository.countByRole(Role.USER);
    }
    public long countPendingApprovals() {
        return userRepository.countPendingApprovals();
    }
    public long countBlockedUsers()     {
        return userRepository.countBlockedUsers();
    }
    public long countApprovedUsers()    {
        return userRepository.countApprovedUsers();
    }

    // ─────────────────────────────────────────────────────────────
    // MAPPING
    // ─────────────────────────────────────────────────────────────

    public UserResponse mapToResponse(User user) {
        int taken  = attemptRepository.countByUserId(user.getId());
        int passed = attemptRepository.countPassedByUserId(user.getId());
        Double avgScore = attemptRepository
                .avgScoreByUserId(user.getId());
        Double avgPct   = attemptRepository
                .avgPercentageByUserId(user.getId());

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole().name())
                .provider(user.getProvider())
                .profilePicture(user.getProfilePicture())
                .bio(user.getBio())
                .approved(user.isApproved())
                .blocked(user.isBlocked())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .mobileVerified(user.isMobileVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .totalExamsTaken(taken)
                .totalExamsPassed(passed)
                .totalExamsFailed(taken - passed)
                .averageScore(avgScore != null
                        ? Math.round(avgScore * 10.0) / 10.0 : 0.0)
                .averagePercentage(avgPct != null
                        ? Math.round(avgPct * 10.0) / 10.0 : 0.0)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
    }

    private String computePerformanceBand(double pct) {
        if (pct >= 90) return "Excellent";
        if (pct >= 75) return "Good";
        if (pct >= 50) return "Average";
        return "Below Average";
    }
}