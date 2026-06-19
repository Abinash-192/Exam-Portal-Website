package com.examportal.repository;

import com.examportal.model.Role;
import com.examportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByMobile(String mobile);

    boolean existsByEmail(String email);
    boolean existsByMobile(String mobile);


    List<User> findByRole(Role role);

    List<User> findByEmailVerifiedTrueAndApprovedFalseAndBlockedFalse();

    List<User> findByBlocked(boolean blocked);

    @Query("SELECT COUNT(u) from User u WHERE u.role = :role")
    long countByRole(Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.approved = false AND u.blocked = false AND u.emailVerified = true")
    long countPendingApprovals();
}
