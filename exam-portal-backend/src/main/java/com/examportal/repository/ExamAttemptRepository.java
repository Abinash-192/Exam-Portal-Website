package com.examportal.repository;

import com.examportal.model.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {

    List<ExamAttempt> findByUserIdOrderByAttemptedAtDesc(Long userId);
    List<ExamAttempt> findByExamIdOrderByAttemptedAtDesc(Long examId);

    int countByUserId(Long userId);

    @Query("SELECT CAST(COUNT(a) AS long) " +
            "FROM ExamAttempt a WHERE a.exam.id = :examId")
    Long countByExamId(Long examId);

    @Query("SELECT COUNT(a) FROM ExamAttempt a "+ "WHERE a.user.id = :userId AND a.passed = true")
    int countPassedByUserId(Long userId);

    @Query("SELECT COUNT(a) FROM ExamAttempt a "+ "WHERE a.exam.id = :examId AND a.passed = true")
    Long countPassedByExamId(Long examId);

    @Query("SELECT AVG(a.scoreObtained) FROM ExamAttempt a "+ "WHERE a.exam.id = :examId")
    Double avgScoreByExamId(Long examId);

    @Query("SELECT AVG(a.scoreObtained) FROM ExamAttempt a "+ "WHERE a.user.id  = :userId")
    Double avgScoreByUserId(Long userId);

    @Query("SELECT AVG(a.percentage) FROM ExamAttempt a "+ "WHERE a.user.id  = :userId")
    Double avgPercentageByUserId(Long userId);

    @Query("SELECT COUNT(a) FROM ExamAttempt a")
    long countTotalAttempts();

    @Query("SELECT ROUND("+ "COUNT(CASE WHEN a.passed = true THEN 1 END) * 100.0 / COUNT(a)" + ", 2) FROM ExamAttempt a")
    Double getOverallPassRate();

    @Query("SELECT a FROM ExamAttempt a "+ "WHERE a.resultEmailSent = false "+ "AND a.status = 'COMPLETED'")
    List<ExamAttempt> findPendingEmailDispatch();

    @Query("SELECT AVG(a.percentage) FROM ExamAttempt a " +
            "WHERE a.exam.id = :examId")
    Double avgPercentageByExamId(Long examId);

    @Query("SELECT MAX(a.scoreObtained) FROM ExamAttempt a " +
            "WHERE a.exam.id = :examId")
    Integer maxScoreByExamId(Long examId);

    @Query("SELECT MIN(a.scoreObtained) FROM ExamAttempt a " +
            "WHERE a.exam.id = :examId")
    Integer minScoreByExamId(Long examId);
}
