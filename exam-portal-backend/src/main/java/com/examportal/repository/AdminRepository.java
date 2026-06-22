package com.examportal.repository;

import com.examportal.model.Role;
import com.examportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<User,Long> {

    List<User> findByRole(Role role);

    Optional<User> findByEmailAndRole(String email , Role role);

    List<User> findRecentlyRegistered(LocalDateTime since);

    long countPendingApprovals();
    long countByRole(Role role);
    long countBlockedUsers();
}
