package com.examportal.repository;

import com.examportal.model.AttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttemptAnswerRepository
        extends JpaRepository<AttemptAnswer, Long> {

    // ── All answers for an attempt ────────────────────────────────
    List<AttemptAnswer> findByAttemptId(Long attemptId);

    // ── Find specific answer for a question in attempt ────────────
    Optional<AttemptAnswer> findByAttemptIdAndQuestionId(
            Long attemptId, Long questionId);

    // ── Count correct answers for an attempt ──────────────────────
    @Query("SELECT COUNT(a) FROM AttemptAnswer a " +
            "WHERE a.attempt.id = :attemptId " +
            "AND   a.correct    = true")
    int countCorrectByAttemptId(Long attemptId);

    // ── Delete all answers for an attempt ─────────────────────────
    @Modifying
    @Query("DELETE FROM AttemptAnswer a " +
            "WHERE a.attempt.id = :attemptId")
    void deleteByAttemptId(Long attemptId);
}