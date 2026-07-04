//package com.examportal.repository;
//
//import com.examportal.model.Role;
//import com.examportal.model.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface AdminRepository extends JpaRepository<User,Long> {
//
//    List<User> findByRole(Role role);
//
//    Optional<User> findByEmailAndRole(String email , Role role);
//
//    @Query("SELECT u from User u " +
//            "WHERE u.createdAt >= :since " +
//            "AND u.role = 'USER' "+
//            "ORDER BY u.createdAt DESC")
//    List<User> findRecentlyRegistered(LocalDateTime since);
//
//    @Query("SELECT COUNT(u) FROM User u "+
//            "WHERE u.approved = false "+
//            "AND u.blocked = false "+
//            "AND u.emailVerified = true " +
//            "AND u.role = 'USER' ")
//    long countPendingApprovals();
//
//    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role ")
//    long countByRole(Role role);
//
//    @Query("SELECT COUNT(u) FROM User u "+
//            "WHERE u.blocked = true AND u.role = 'USER' ")
//    long countBlockedUsers();
//
//    @Query("SELECT COUNT(u) FROM User u "+ "WHERE u.approved = true AND U.role = 'USER'")
//    long countApprovedUsers();
//
//    @Query(value = "SELECT DATE(created_At) as reg_date , COUNT(*) as count "
//            + "FROM users "+ "WHERE created_at >= DATE_SUB(NOW(), " +
//            "INTERVAL 7 DAY) "+ "AND role = 'USER' "+ "GROUP BY DATE(created_at) "+
//            "ORDER BY reg_date ASC" , nativeQuery = true)
//    List<Object[]> getRegistrationCountLast7Days();
//
//    @Query("SELECT u FROM User u "+
//            "WHERE u.role = 'USER' " +
//            "AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
//            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
//    List<User> searchUsers(String keyword);
//}


package com.examportal.repository;

import com.examportal.model.Role;
import com.examportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<User, Long> {

    // ── Find all admins ───────────────────────────────────────────
    List<User> findByRoleOrderByCreatedAtDesc(Role role);

    // ── Find admin by email ───────────────────────────────────────
    Optional<User> findByEmailAndRole(String email, Role role);

    // ── Count by role ─────────────────────────────────────────────
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(Role role);

    // ── Count pending approvals ───────────────────────────────────
    @Query("SELECT COUNT(u) FROM User u " +
            "WHERE u.approved      = false " +
            "AND   u.blocked       = false " +
            "AND   u.emailVerified = true  " +
            "AND   u.role          = 'USER'")
    long countPendingApprovals();

    // ── Count blocked users ───────────────────────────────────────
    @Query("SELECT COUNT(u) FROM User u " +
            "WHERE u.blocked = true AND u.role = 'USER'")
    long countBlockedUsers();

    // ── Count approved users ──────────────────────────────────────
    @Query("SELECT COUNT(u) FROM User u " +
            "WHERE u.approved = true AND u.role = 'USER'")
    long countApprovedUsers();

    // ── Recently registered students ──────────────────────────────
    @Query("SELECT u FROM User u " +
            "WHERE u.role       = 'USER' " +
            "AND   u.createdAt >= :since " +
            "ORDER BY u.createdAt DESC")
    List<User> findRecentlyRegistered(LocalDateTime since);

    // ── Registration chart data (last 7 days) ─────────────────────
    @Query(value =
            "SELECT DATE(created_at) AS reg_date, COUNT(*) AS cnt " +
                    "FROM users " +
                    "WHERE role = 'USER' " +
                    "AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                    "GROUP BY DATE(created_at) " +
                    "ORDER BY reg_date ASC",
            nativeQuery = true)
    List<Object[]> getRegistrationCountLast7Days();

    // ── Search students by name or email ──────────────────────────
    @Query("SELECT u FROM User u " +
            "WHERE u.role = 'USER' " +
            "AND ( LOWER(u.name)  LIKE LOWER(CONCAT('%',:kw,'%')) " +
            "  OR  LOWER(u.email) LIKE LOWER(CONCAT('%',:kw,'%')))")
    List<User> searchStudents(String kw);
}