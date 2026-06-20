//package com.examportal.repository;
//
//import com.examportal.model.Role;
//import com.examportal.model.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface UserRepository extends JpaRepository<User, Long> {
//
//    Optional<User> findByEmail(String email);
//    Optional<User> findByMobile(String mobile);
//
//    boolean existsByEmail(String email);
//    boolean existsByMobile(String mobile);
//
//
//    List<User> findByRole(Role role);
//
//    List<User> findByEmailVerifiedTrueAndApprovedFalseAndBlockedFalse();
//
//    List<User> findByBlocked(boolean blocked);
//
//    @Query("SELECT COUNT(u) from User u WHERE u.role = :role")
//    long countByRole(Role role);
//
//    @Query("SELECT COUNT(u) FROM User u WHERE u.approved = false AND u.blocked = false AND u.emailVerified = true")
//    long countPendingApprovals();
//}

package com.examportal.repository;

import com.examportal.model.Role;
import com.examportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ── Basic finders ─────────────────────────────────────────────
    Optional<User> findByEmail(String email);
    Optional<User> findByMobile(String mobile);
    Optional<User> findByEmailAndRole(String email, Role role);

    // ── Existence checks ──────────────────────────────────────────
    boolean existsByEmail(String email);
    boolean existsByMobile(String mobile);

    // ── Role-based listing ────────────────────────────────────────
    List<User> findByRole(Role role);
    List<User> findByRoleOrderByCreatedAtDesc(Role role);

    // ── Status-based listing ──────────────────────────────────────
    List<User> findByBlocked(boolean blocked);
    List<User> findByApproved(boolean approved);
    List<User> findByEnabled(boolean enabled);

    // ── Pending approval:
    //    email verified + not approved + not blocked ────────────────
    List<User> findByEmailVerifiedTrueAndApprovedFalseAndBlockedFalse();

    // ── Count queries ─────────────────────────────────────────────
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(Role role);

    @Query("SELECT COUNT(u) FROM User u " +
            "WHERE u.approved = false " +
            "AND u.blocked    = false " +
            "AND u.emailVerified = true " +
            "AND u.role = 'USER'")
    long countPendingApprovals();

    @Query("SELECT COUNT(u) FROM User u " +
            "WHERE u.blocked = true AND u.role = 'USER'")
    long countBlockedUsers();

    @Query("SELECT COUNT(u) FROM User u " +
            "WHERE u.approved = true AND u.role = 'USER'")
    long countApprovedUsers();

    // ── Search by name or email ───────────────────────────────────
    @Query("SELECT u FROM User u " +
            "WHERE u.role = 'USER' " +
            "AND (LOWER(u.name)  LIKE LOWER(CONCAT('%', :kw, '%')) " +
            " OR  LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%')))")
    List<User> searchByNameOrEmail(String kw);

    // ── Recently registered (last N days) ─────────────────────────
    @Query("SELECT u FROM User u " +
            "WHERE u.role = 'USER' " +
            "AND u.createdAt >= :since " +
            "ORDER BY u.createdAt DESC")
    List<User> findRecentlyRegistered(LocalDateTime since);

    // ── Registration count per day (last 7 days) ──────────────────
    @Query(value =
            "SELECT DATE(created_at) AS reg_date, COUNT(*) AS cnt " +
                    "FROM users " +
                    "WHERE role = 'USER' " +
                    "AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                    "GROUP BY DATE(created_at) " +
                    "ORDER BY reg_date ASC",
            nativeQuery = true)
    List<Object[]> getRegistrationCountLast7Days();

    // ── Update last login timestamp ───────────────────────────────
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :time WHERE u.id = :id")
    void updateLastLoginAt(Long id, LocalDateTime time);

    // ── Update profile picture ────────────────────────────────────
    @Modifying
    @Query("UPDATE User u SET u.profilePicture = :url WHERE u.id = :id")
    void updateProfilePicture(Long id, String url);
}