package com.examportal.service;

import com.examportal.dto.response.AttemptResultResponse;
import com.examportal.dto.response.UserResponse;
import com.examportal.dto.response.UserStatsResponse;
import com.examportal.exception.ValidationException;
import com.examportal.model.AdminAction.ActionType;
import com.examportal.model.Role;
import com.examportal.model.User;
import com.examportal.repository.AdminActionRepository;
import com.examportal.repository.AdminRepository;
import com.examportal.repository.ExamRepository;
import com.examportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);
    private final UserRepository  userRepository;
    private final AdminRepository adminRepository;
    private final AdminActionRepository actionRepository;
    private final ExamRepository examRepository;
    private final UserService  userService;
    private final EmailService  emailService;
    private final SimpMessagingTemplate ws;


    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(){

        return userRepository.findAll()
                .stream().map(userService::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllStudents(){

        return userRepository.findByRole(Role.USER)
                .stream().map(userService::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getPendingUsers(){

        return userRepository.findByEmailVerifiedTrueAndApprovedFalseAndBlockedFalse()
                .stream().map(userService::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getBlockedUsers(){

        return userRepository.findByBlocked(true)
                .stream().map(userService::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUserById(Long id){

        return userService.mapToResponse(findUserOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String keyword){

        return adminRepository.searchUsers(keyword)
                .stream().map(userService::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats(Long userId){

        User user = findUserOrThrow(userId);
        int totalTaken = attemptRepository.countByUserId(userId);
        int totalPassed = attemptRepository.countPassedByUserId(userId);
        int totalFailed = totalTaken - totalPassed;

        Double avgScore = attemptRepository.avgScoreByUserId(userId);
        Double avgPct = attemptRepository.avgPercentageByUserId(userId);

        LocalDateTime lastExam = attemptRepository
                .findByUserIdOderByAttemptedAtDesc(userId)
                .stream()
                .findFirst()
                .map(a -> a.getAttemptedAt())
                .orElse(null);

        List<AttemptResultResponse> history = attemptRepository
                .findByUserIdOrderByAttemptedAtDesc(userId)
                .stream()
                .map(a -> AttemptResultResponse.builder()
                        .attemptId(a.getId())
                        .examTitle(a.getExam().getTitle())
                        .category(a.getExam().getCategory())
                        .scoreObtained(a.getScoreObtained())
                        .totalMarks(a.getTotalMarks())
                        .percentage(a.getPercentage())
                        .passed(a.isPassed())
                        .correctAnswers(a.getCorrectAnswers())
                        .wrongAnswers(a.getWrongAnswers())
                        .timeTakenSeconds(a.getTimeTakenSeconds())
                        .performanceBand(computePerformanceBand(a.getPercentage()))
                        .attemptedAt(a.getAttemptedAt())
                        .build())
                .collect(Collectors.toList());

          return UserStatsResponse.builder()
                  .userId(user.getId())
                  .name(user.getName())
                  .email(user.getEmail())
                  .mobile(user.getMobile())
                  .role(user.getRole().name())
                  .provider(user.getProvider())
                  .approved(user.isApproved())
                  .blocked(user.isBlocked())
                  .emailVerified(user.isEmailVerified())
                  .totalExamsTaken(totalTaken)
                  .totalExamsPassed(totalPassed)
                  .totalExamsFailed(totalFailed)
                  .averageScore(avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 0.0)
                  .averagePercentage(avgPct != null ? Math.round(avgPct * 10.0) / 10.0 : 0.0)
                  .bestPerformanceBand(computerPerformanceBand(avgPct != null ? avgPct : 0.0))
                  .joinedAt(user.getCreatedAt())
                  .lastExamDate(lastExam)
                  .examHistory(history)
                  .build();
    }

     public UserResponse approveUser(Long id ,String reason){

         User user = findUserOrThrow(id);
         User admin = getCurrentAdmin();

         if(user.isApproved()) {
             throw new ValidationException("User [" + user.getEmail() + "] is already approved.");
         }
         if (user.isBlocked()) {
             throw new ValidationException("Cannot approved a blocked user. Unblock first.");
         }

         if (!user.isEmailVerified()) {
             throw  new ValidationException("Cannot approve a user whose email is not yet verified.");
         }

         user.setApproved(true);
         user.setEnabled(true);
         userRepository.save(user);

         logAction(admin,user, ActionType.APPROVE_USER,reason != null ? reason : "Approved by Admin");

         broadcastAdminEvent("USER_APPROVED", Map.of("userId", user.getId(),
                 "userName", user.getName() ,
                 "email" , user.getEmail()));

         emailService.sendApprovalEmail(user.getEmail() , user.getName());
         log.info("Admin [{}] approved user [{}]" , admin.getEmail(),user.getEmail());
         return  userService.mapToResponse(user);
     }

     @Transactional
     public UserResponse blockUser(Long id, String reason){

        User user = findUserOrThrow(id);
        User admin = getCurrentAdmin();

         if (user.getRole() == Role.ADMIN) {
             throw  new ValidationException("Admin Accounts can't be blocked.");
         }

         if (user.isBlocked()) {
             throw  new ValidationException("User [" + user.getEmail() + "] is already blocked.");
         }

         user.setBlocked(true);
         user.setEnabled(false);
         userRepository.save(user);

         logAction(admin, user, ActionType.BLOCK_USER, reason != null ? reason : "Blocked by Admin");
         broadcastAdminEvent("USER BLOCKED", Map.of("UserId", user.getId(),
                 "userName", user.getName(),
                 "email", user.getEmail()));

         emailService.sendBlockedEmail(user.getEmail() , user.getName());
         log.info("Admin [{}] blocked user [{}] ", admin.getEmail(), user.getEmail());
         return userService.mapToResponse(user);
     }

     @Transactional
     public UserResponse unBlockUser(Long id, String reason){
        User user = findUserOrThrow(id);
        User admin = getCurrentAdmin();

         if (!user.isBlocked()) {
            throw new ValidationException("User ["+ user.getEmail() +"] is not blocked.");
         }

         user.setBlocked(false);
         user.setEnabled(true);
         userRepository.save(user);

         logAction(admin, user, ActionType.UNBLOCK_USER, reason != null ? reason : "Unblocked by admin");
         broadCastAdminEvent("USER_UNBLOCKED", Map.of(
                 "userId", user.getId(),"userName",
                 user.getName(),
                 "email", user.getEmail()));

         emailService.sendUnblockedEmail(user.getEmail(), user.getName());
         log.info("Admin [{}] unblocked user [{}] ", admin.getEmail(),user.getEmail());
         return userService.mapToResponse(user);
     }

     @Transactional
     public void deleteUser(Long id, String reason){

        User user = findUserOrThrow(id);
        User admin = getCurrentAdmin();

         if (user.getRole() == Role.ADMIN) {
             throw new ValidationException("Admin accounts cannot be deleted.");
         }

         logAction(admin, user, ActionType.DELETE_USER, reason != null ? reason : "Deleted by admin");
         userRepository.delete(user);
         broadcastAdminEvent("USER_DELETED" , Map.of(
                 "userId", user.getId(),
                 "userName", user.getName(),
                 "email", user.getEmail()));
         log.info("Admin [{}] deleted user [{}] ", admin.getEmail(),user.getEmail());
     }

     

}
