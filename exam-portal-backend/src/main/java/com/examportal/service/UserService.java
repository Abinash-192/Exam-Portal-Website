package com.examportal.service;

import com.examportal.dto.response.UserResponse;
import com.examportal.exception.ResourceNotFoundException;
import com.examportal.model.Role;
import org.springframework.transaction.annotation.Transactional;
import com.examportal.model.User;
import com.examportal.repository.ExamAttemptRepository;
import com.examportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ExamAttemptRepository attemptRepository;

    public UserResponse getById(Long id){

        return  mapToResponse(findOrThrow(id));
    }

    public UserResponse getByEmail(String email){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No user with email: " +email));
        return mapToResponse(user);
    }

    public List<UserResponse> getAllUsers(){

        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getPendingUsers(){

        return userRepository.findByEmailVerifiedTrueAndApprovedFalseAndBlockedFalse()
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getBlockedUsers(){
        return userRepository.findByBlocked(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse approveUser(Long id){

        User user = findOrThrow(id);
        user.setApproved(true);
        user.setEnabled(true);
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse blockUser(Long id){

        User user = findOrThrow(id);
        if (user.getRole() == Role.ADMIN) {
            throw new com.examportal.exception.ValidationException(
                    "Admin accounts cannot be blocked."
            );

        }
        user.setBlocked(true);
        user.setEnabled(false);
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse unblockUser(Long id){

        User user = findOrThrow(id);
        user.setBlocked(false);
        user.setEnabled(true);
        return  mapToResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id){

        User user = findOrThrow(id);
        if (user.getRole() == Role.ADMIN) {
            throw new com.examportal.exception.ValidationException(

                    "Admin accounts cannot be deleted."
            );
        }
        userRepository.delete(user);
    }

    public long countUsers(){

        return userRepository.countByRole(Role.USER);
    }

    public long countPendingUsers(){
        return userRepository.countPendingApprovals();
    }

    public UserResponse mapToResponse(User user){

        int totalAttempts = attemptRepository.countByUserId(user.getId());
        int passedAttempts = attemptRepository.countPassedUserId(user.getId());

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole().name())
                .profilePicture(user.getProfilePicture())
                .provider(user.getProvider())
                .approved(user.isApproved())
                .blocked(user.isBlocked())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .mobileVerified(user.isMobileVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .totalExamsTaken(totalAttempts)
                .totalExamsPassed(passedAttempts)
                .build();
    }

    private User findOrThrow(Long id){

        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id : "+id));
    }
}
