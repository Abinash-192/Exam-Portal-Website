//package com.examportal.repository;
//
//import com.examportal.model.ExamAttempt;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//
//import java.util.List;
//
//public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
//
//    List<ExamAttempt> findByUserIdOrderByAttemptedAtDesc(Long userId);
//    List<ExamAttempt> findByExamIdOrderByAttemptedAtDesc(Long examId);
//
//    int countByUserId(Long userId);
//
//    @Query("SELECT CAST(COUNT(a) AS long) " +
//            "FROM ExamAttempt a WHERE a.exam.id = :examId")
//    Long countByExamId(Long examId);
//
//    @Query("SELECT COUNT(a) FROM ExamAttempt a "+ "WHERE a.user.id = :userId AND a.passed = true")
//    int countPassedByUserId(Long userId);
//
//    @Query("SELECT COUNT(a) FROM ExamAttempt a "+ "WHERE a.exam.id = :examId AND a.passed = true")
//    Long countPassedByExamId(Long examId);
//
//    @Query("SELECT AVG(a.scoreObtained) FROM ExamAttempt a "+ "WHERE a.exam.id = :examId")
//    Double avgScoreByExamId(Long examId);
//
//    @Query("SELECT AVG(a.scoreObtained) FROM ExamAttempt a "+ "WHERE a.user.id  = :userId")
//    Double avgScoreByUserId(Long userId);
//
//    @Query("SELECT AVG(a.percentage) FROM ExamAttempt a "+ "WHERE a.user.id  = :userId")
//    Double avgPercentageByUserId(Long userId);
//
//    @Query("SELECT COUNT(a) FROM ExamAttempt a")
//    long countTotalAttempts();
//
//    @Query("SELECT ROUND("+ "COUNT(CASE WHEN a.passed = true THEN 1 END) * 100.0 / COUNT(a)" + ", 2) FROM ExamAttempt a")
//    Double getOverallPassRate();
//
//    @Query("SELECT a FROM ExamAttempt a "+ "WHERE a.resultEmailSent = false "+ "AND a.status = 'COMPLETED'")
//    List<ExamAttempt> findPendingEmailDispatch();
//
//    @Query("SELECT AVG(a.percentage) FROM ExamAttempt a " +
//            "WHERE a.exam.id = :examId")
//    Double avgPercentageByExamId(Long examId);
//
//    @Query("SELECT MAX(a.scoreObtained) FROM ExamAttempt a " +
//            "WHERE a.exam.id = :examId")
//    Integer maxScoreByExamId(Long examId);
//
//    @Query("SELECT MIN(a.scoreObtained) FROM ExamAttempt a " +
//            "WHERE a.exam.id = :examId")
//    Integer minScoreByExamId(Long examId);
//}


package com.examportal.repository;

import com.examportal.model.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamAttemptRepository
        extends JpaRepository<ExamAttempt, Long> {

    // ── Basic finders ─────────────────────────────────────────────
    List<ExamAttempt> findByUserIdOrderByAttemptedAtDesc(
            Long userId);

    List<ExamAttempt> findByExamIdOrderByAttemptedAtDesc(
            Long examId);

    Optional<ExamAttempt> findByIdAndUserId(
            Long id, Long userId);

    Optional<ExamAttempt> findByUserIdAndExamIdAndStatus(
            Long userId, Long examId,
            ExamAttempt.AttemptStatus status);

    boolean existsByUserIdAndExamIdAndStatusIn(
            Long userId, Long examId,
            List<ExamAttempt.AttemptStatus> statuses);

    // ── Count ─────────────────────────────────────────────────────
    // Used in UserService.mapToResponse()
    int countByUserId(Long userId);

    @Query("SELECT CAST(COUNT(a) AS long) " +
            "FROM ExamAttempt a WHERE a.exam.id = :examId")
    long countByExamId(Long examId);

    // ── Passed counts ─────────────────────────────────────────────
    // Used in UserService.mapToResponse() and getProfile()
    @Query("SELECT COUNT(a) FROM ExamAttempt a " +
            "WHERE a.user.id = :userId AND a.passed = true")
    int countPassedByUserId(Long userId);

    @Query("SELECT COUNT(a) FROM ExamAttempt a " +
            "WHERE a.exam.id = :examId AND a.passed = true")
    long countPassedByExamId(Long examId);

    // ── Averages ──────────────────────────────────────────────────
    // Used in UserService.mapToResponse() and getDashboard()
    @Query("SELECT AVG(a.scoreObtained) FROM ExamAttempt a " +
            "WHERE a.user.id = :userId")
    Double avgScoreByUserId(Long userId);

    @Query("SELECT AVG(a.percentage) FROM ExamAttempt a " +
            "WHERE a.user.id = :userId")
    Double avgPercentageByUserId(Long userId);

    @Query("SELECT AVG(a.scoreObtained) FROM ExamAttempt a " +
            "WHERE a.exam.id = :examId")
    Double avgScoreByExamId(Long examId);

    @Query("SELECT AVG(a.percentage) FROM ExamAttempt a " +
            "WHERE a.exam.id = :examId")
    Double avgPercentageByExamId(Long examId);

    // ── Max / Min ─────────────────────────────────────────────────
    @Query("SELECT MAX(a.scoreObtained) FROM ExamAttempt a " +
            "WHERE a.exam.id = :examId")
    Integer maxScoreByExamId(Long examId);

    @Query("SELECT MIN(a.scoreObtained) FROM ExamAttempt a " +
            "WHERE a.exam.id = :examId")
    Integer minScoreByExamId(Long examId);

    // ── Global stats ──────────────────────────────────────────────
    @Query("SELECT COUNT(a) FROM ExamAttempt a")
    long countTotalAttempts();

    @Query("SELECT ROUND(" +
            "COUNT(CASE WHEN a.passed = true THEN 1 END) " +
            "* 100.0 / COUNT(a), 2) " +
            "FROM ExamAttempt a")
    Double getOverallPassRate();

    // ── Timer ─────────────────────────────────────────────────────
    @Query("SELECT a FROM ExamAttempt a " +
            "WHERE a.status    = 'IN_PROGRESS' " +
            "AND   a.expiresAt <= :now")
    List<ExamAttempt> findExpiredAttempts(LocalDateTime now);

    @Query("SELECT a FROM ExamAttempt a " +
            "WHERE a.status    = 'IN_PROGRESS' " +
            "AND   a.expiresAt BETWEEN :now AND :fiveMinLater")
    List<ExamAttempt> findAttemptExpiringSoon(
            LocalDateTime now, LocalDateTime fiveMinLater);

    // ── Pending email ─────────────────────────────────────────────
    @Query("SELECT a FROM ExamAttempt a " +
            "WHERE a.resultEmailSent = false " +
            "AND   a.status IN ('COMPLETED','TIMED_OUT')")
    List<ExamAttempt> findPendingEmailDispatch();

    // ── Update status ─────────────────────────────────────────────
    @Modifying
    @Query("UPDATE ExamAttempt a " +
            "SET a.status = :status WHERE a.id = :id")
    void updateStatus(Long id,
                      ExamAttempt.AttemptStatus status);
}