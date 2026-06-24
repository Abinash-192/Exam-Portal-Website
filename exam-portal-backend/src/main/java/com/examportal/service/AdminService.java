package com.examportal.service;

import com.examportal.dto.response.UserResponse;
import com.examportal.model.Role;
import com.examportal.repository.AdminActionRepository;
import com.examportal.repository.AdminRepository;
import com.examportal.repository.ExamRepository;
import com.examportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

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

    
}
