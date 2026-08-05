package com.examportal.service;

import com.examportal.dto.request.SaveAnswerRequest;
import com.examportal.dto.request.StartAttemptRequest;
import com.examportal.dto.request.SubmitAttemptRequest;
import com.examportal.dto.response.AttemptResultResponse;
import com.examportal.dto.response.AttemptStartResponse;
import com.examportal.dto.response.QuestionResponse;
import com.examportal.dto.response.TimerStatusResponse;
import com.examportal.exception.ResourceNotFoundException;
import com.examportal.exception.ValidationException;
import com.examportal.model.*;
import com.examportal.model.ExamAttempt.AttemptStatus;
import com.examportal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamAttemptService {

    private final ExamAttemptRepository   attemptRepository;
    private final ExamRepository          examRepository;
    private final UserRepository          userRepository;
    private final QuestionRepository      questionRepository;
    private final AttemptAnswerRepository answerRepository;
    private final NotificationRepository  notificationRepository;
    private final EmailService            emailService;
    private final SimpMessagingTemplate   ws;
    private final QuestionService         questionService;

    // ═════════════════════════════════════════════════════════════
    // START ATTEMPT — initialise timer
    // ═════════════════════════════════════════════════════════════

    @Transactional
    public AttemptStartResponse startAttempt(
            Long userId, StartAttemptRequest req) {

        User user = findUserOrThrow(userId);

        // ── Admin approval guard ───────────────────────────────────
        if (!user.isApproved())
            throw new ValidationException(
                    "Your account is not approved by admin. " +
                            "You cannot take any exam until admin " +
                            "approves your registration.");

        if (user.isBlocked())
            throw new ValidationException(
                    "Your account is blocked. " +
                            "Contact admin for support.");

        Exam exam = examRepository
                .findById(req.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exam not found: " + req.getExamId()));

        if (!exam.isActive())
            throw new ValidationException(
                    "This exam is not currently available.");

        // ── Resume if IN_PROGRESS attempt exists ──────────────────
        Optional<ExamAttempt> existing =
                attemptRepository.findByUserIdAndExamIdAndStatus(
                        userId, req.getExamId(),
                        AttemptStatus.IN_PROGRESS);

        if (existing.isPresent()) {
            ExamAttempt resume = existing.get();
            List<Question> questions = questionRepository
                    .findByExamIdOrderByQuestionOrder(exam.getId());
            log.info("User [{}] resumed attempt [{}]",
                    userId, resume.getId());
            return buildStartResponse(resume, exam, questions);
        }

        // ── Prevent re-attempt ────────────────────────────────────
        boolean alreadyDone =
                attemptRepository.existsByUserIdAndExamIdAndStatusIn(
                        userId, req.getExamId(),
                        List.of(AttemptStatus.COMPLETED,
                                AttemptStatus.TIMED_OUT));

        if (alreadyDone)
            throw new ValidationException(
                    "You have already attempted this exam.");

        // ── Timer setup ───────────────────────────────────────────
        LocalDateTime now       = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(
                exam.getDurationMintues());
        int allowedSeconds      = exam.getDurationMintues() * 60;

        // ── Create attempt ────────────────────────────────────────
        ExamAttempt attempt = ExamAttempt.builder()
                .user(user)
                .exam(exam)
                .status(AttemptStatus.IN_PROGRESS)
                .startedAt(now)
                .expiresAt(expiresAt)
                .allowedTimeSeconds(allowedSeconds)
                .totalMarks(exam.getTotalMarks())
                .build();

        ExamAttempt saved = attemptRepository.save(attempt);

        List<Question> questions = questionRepository
                .findByExamIdOrderByQuestionOrder(exam.getId());

        // ── Notify admin exam started ─────────────────────────────
        Map<String, Object> startPayload = new HashMap<>();
        startPayload.put("event",     "EXAM_STARTED");
        startPayload.put("user",      user.getName());
        startPayload.put("email",     user.getEmail());
        startPayload.put("exam",      exam.getTitle());
        startPayload.put("attemptId", saved.getId());
        startPayload.put("expiresAt", expiresAt.toString());
        sendToAdmin(startPayload);

        log.info("Attempt [{}] started by [{}] expires [{}]",
                saved.getId(), userId, expiresAt);

        return buildStartResponse(saved, exam, questions);
    }

    // ═════════════════════════════════════════════════════════════
    // SAVE ANSWER (auto-save per question click)
    // ═════════════════════════════════════════════════════════════

    @Transactional
    public String saveAnswer(Long userId,
                             SaveAnswerRequest req) {

        ExamAttempt attempt = attemptRepository
                .findByIdAndUserId(req.getAttemptId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attempt not found."));

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS)
            throw new ValidationException(
                    "Exam is not in progress. Status: " +
                            attempt.getStatus());

        if (LocalDateTime.now().isAfter(attempt.getExpiresAt()))
            throw new ValidationException(
                    "Exam time has expired. " +
                            "Your exam will be auto-submitted.");

        // ── Upsert answer ─────────────────────────────────────────
        Optional<AttemptAnswer> existing =
                answerRepository.findByAttemptIdAndQuestionId(
                        req.getAttemptId(), req.getQuestionId());

        if (existing.isPresent()) {
            AttemptAnswer ans = existing.get();
            ans.setSelectedOptionId(req.getSelectedOptionId());
            answerRepository.save(ans);
        } else {
            answerRepository.save(AttemptAnswer.builder()
                    .attempt(attempt)
                    .questionId(req.getQuestionId())
                    .selectedOptionId(req.getSelectedOptionId())
                    .correct(false)
                    .build());
        }

        return "Answer saved.";
    }

    // ═════════════════════════════════════════════════════════════
    // GET TIMER STATUS (frontend polls every 30 sec)
    // ═════════════════════════════════════════════════════════════

    public TimerStatusResponse getTimerStatus(
            Long userId, Long attemptId) {

        ExamAttempt attempt = attemptRepository
                .findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attempt not found."));

        LocalDateTime now = LocalDateTime.now();
        long elapsed   = ChronoUnit.SECONDS.between(
                attempt.getStartedAt(), now);
        long remaining = ChronoUnit.SECONDS.between(
                now, attempt.getExpiresAt());

        if (remaining < 0) remaining = 0;

        double progress = Math.min(
                (double) elapsed
                        / attempt.getAllowedTimeSeconds() * 100, 100);

        boolean expired  = now.isAfter(attempt.getExpiresAt());
        boolean warning  = remaining <= 300; // < 5 min
        boolean critical = remaining <= 60;  // < 1 min

        return TimerStatusResponse.builder()
                .attemptId(attempt.getId())
                .status(attempt.getStatus().name())
                .allowedTimeSeconds(attempt.getAllowedTimeSeconds())
                .elapsedSeconds((int) elapsed)
                .remainingSeconds((int) remaining)
                .progressPercentage(
                        Math.round(progress * 10.0) / 10.0)
                .isExpired(expired)
                .isWarning(warning)
                .isCritical(critical)
                .autoSubmitted(attempt.isAutoSubmitted())
                .startedAt(attempt.getStartedAt())
                .expiresAt(attempt.getExpiresAt())
                .serverTime(now.toString())
                .build();
    }

    // ═════════════════════════════════════════════════════════════
    // SUBMIT ATTEMPT (manual or auto)
    // ═════════════════════════════════════════════════════════════

    @Transactional
    public AttemptResultResponse submitAttempt(
            Long userId, SubmitAttemptRequest req) {

        ExamAttempt attempt = attemptRepository
                .findByIdAndUserId(req.getAttemptId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attempt not found."));

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS)
            throw new ValidationException(
                    "Exam already submitted. Status: " +
                            attempt.getStatus());

        User user = attempt.getUser();
        Exam exam = attempt.getExam();

        if (user.isBlocked())
            throw new ValidationException(
                    "Your account is blocked.");

        // ── Determine final status ────────────────────────────────
        boolean isTimedOut =
                "TIMED_OUT".equalsIgnoreCase(req.getStatus())
                        || LocalDateTime.now().isAfter(
                        attempt.getExpiresAt());

        AttemptStatus finalStatus = isTimedOut
                ? AttemptStatus.TIMED_OUT
                : AttemptStatus.COMPLETED;

        // ── Load questions ────────────────────────────────────────
        List<Question> questions = questionRepository
                .findByExamIdOrderByQuestionOrder(exam.getId());

        // ── Merge DB answers + request answers ────────────────────
        Map<Long, Long> finalAnswers = new HashMap<>();

        answerRepository.findByAttemptId(attempt.getId())
                .forEach(a -> {
                    if (a.getSelectedOptionId() != null)
                        finalAnswers.put(a.getQuestionId(),
                                a.getSelectedOptionId());
                });

        if (req.getAnswers() != null)
            finalAnswers.putAll(req.getAnswers());

        // ── Score calculation ─────────────────────────────────────
        int score = 0, correct = 0, wrong = 0, unanswered = 0;
        List<AttemptAnswer> attemptAnswers = new ArrayList<>();
        List<AttemptResultResponse.AnswerDetail> details =
                new ArrayList<>();

        for (Question q : questions) {
            Long    selected  = finalAnswers.get(q.getId());
            boolean isCorrect = selected != null
                    && selected.equals(q.getCorrectOptionId());

            if (selected == null)   unanswered++;
            else if (isCorrect)   { correct++; score += q.getMarks(); }
            else                    wrong++;

            String selectedText = getOptionText(q, selected);
            String correctText  = getOptionText(
                    q, q.getCorrectOptionId());

            attemptAnswers.add(AttemptAnswer.builder()
                    .attempt(attempt)
                    .questionId(q.getId())
                    .selectedOptionId(selected)
                    .correct(isCorrect)
                    .build());

            details.add(
                    AttemptResultResponse.AnswerDetail.builder()
                            .questionId(q.getId())
                            .questionText(q.getContent())
                            .codeSnippet(q.getCodeSnippet())
                            .language(q.getLanguage())
                            .selectedOptionId(selected)
                            .selectedOptionText(selectedText)
                            .correctOptionId(q.getCorrectOptionId())
                            .correctOptionText(correctText)
                            .correct(isCorrect)
                            .marks(q.getMarks())
                            .explanation(q.getExplanation())
                            .build());
        }

        boolean passed = score >= exam.getPassingMarks();
        double  pct    = exam.getTotalMarks() == 0 ? 0
                : Math.round(score * 1000.0
                / exam.getTotalMarks()) / 10.0;

        // ── Time tracking ─────────────────────────────────────────
        LocalDateTime now       = LocalDateTime.now();
        int timeTaken   = (int) ChronoUnit.SECONDS.between(
                attempt.getStartedAt(), now);
        int timeAllowed = attempt.getAllowedTimeSeconds();
        int remaining   = Math.max(0, timeAllowed - timeTaken);

        // ── Persist updated attempt ───────────────────────────────
        attempt.setScoreObtained(score);
        attempt.setTotalMarks(exam.getTotalMarks());
        attempt.setCorrectAnswers(correct);
        attempt.setWrongAnswers(wrong);
        attempt.setUnanswered(unanswered);
        attempt.setPercentage(pct);
        attempt.setPassed(passed);
        attempt.setStatus(finalStatus);
        attempt.setTimeTakenSeconds(
                Math.min(timeTaken, timeAllowed));
        attempt.setRemainingTimeSeconds(remaining);
        attempt.setAutoSubmitted(isTimedOut);
        attempt.setSubmittedAt(now);
        attemptRepository.save(attempt);

        // ── Clear old answers + save fresh ────────────────────────
        answerRepository.deleteByAttemptId(attempt.getId());
        answerRepository.saveAll(attemptAnswers);

        Double avgScore = attemptRepository
                .avgScoreByExamId(exam.getId());

        // ── Build result response ─────────────────────────────────
        AttemptResultResponse result = AttemptResultResponse.builder()
                .attemptId(attempt.getId())
                .examTitle(exam.getTitle())
                .category(exam.getCategory())
                .difficulty(exam.getDifficulty().name())
                .status(finalStatus.name())
                .allowedTimeSeconds(timeAllowed)
                .timeTakenSeconds(attempt.getTimeTakenSeconds())
                .remainingTimeSeconds(remaining)
                .timeTakenFormatted(
                        formatTime(attempt.getTimeTakenSeconds()))
                .autoSubmitted(isTimedOut)
                .scoreObtained(score)
                .totalMarks(exam.getTotalMarks())
                .percentage(pct)
                .passed(passed)
                .passingMarks(exam.getPassingMarks())
                .correctAnswers(correct)
                .wrongAnswers(wrong)
                .unanswered(unanswered)
                .performanceBand(computeBand(pct))
                .avgScoreForExam(avgScore != null
                        ? Math.round(avgScore * 10.0) / 10.0 : 0)
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .attemptedAt(attempt.getAttemptedAt())
                .answerDetails(details)
                .build();

        // ── Real-time result to student ───────────────────────────
        ws.convertAndSendToUser(
                user.getEmail(),
                "/queue/result",
                result);

        // ── Real-time admin feed ──────────────────────────────────
        Map<String, Object> adminPayload = new HashMap<>();
        adminPayload.put("event",
                isTimedOut ? "EXAM_TIMED_OUT" : "EXAM_SUBMITTED");
        adminPayload.put("user",       user.getName());
        adminPayload.put("email",      user.getEmail());
        adminPayload.put("exam",       exam.getTitle());
        adminPayload.put("score",      score);
        adminPayload.put("total",      exam.getTotalMarks());
        adminPayload.put("percentage", pct);
        adminPayload.put("passed",     passed);
        adminPayload.put("autoSubmit", isTimedOut);
        adminPayload.put("timeTaken",
                formatTime(attempt.getTimeTakenSeconds()));
        sendToAdmin(adminPayload);

        // ── Send result email ─────────────────────────────────────
        emailService.sendResultEmail(user, exam, result);
        attempt.setResultEmailSent(true);
        attemptRepository.save(attempt);

        // ── Send timed-out email ──────────────────────────────────
        if (isTimedOut) {
            emailService.sendTimedOutEmail(
                    user.getEmail(),
                    user.getName(),
                    exam.getTitle(),
                    result);
        }

        // ── In-app notification ───────────────────────────────────
        saveNotification(user, exam, passed, attempt.getId());

        log.info("Attempt [{}] submitted status=[{}] " +
                        "score=[{}/{}] passed=[{}] auto=[{}]",
                attempt.getId(), finalStatus,
                score, exam.getTotalMarks(),
                passed, isTimedOut);

        return result;
    }

    // ═════════════════════════════════════════════════════════════
    // AUTO-SUBMIT EXPIRED EXAMS (every 30 seconds)
    // ═════════════════════════════════════════════════════════════

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void autoSubmitExpiredExams() {
        List<ExamAttempt> expired = attemptRepository
                .findExpiredAttempts(LocalDateTime.now());

        if (!expired.isEmpty())
            log.info("Auto-submitting [{}] expired attempt(s)",
                    expired.size());

        for (ExamAttempt attempt : expired) {
            try {
                SubmitAttemptRequest req =
                        new SubmitAttemptRequest();
                req.setExamId(attempt.getExam().getId());
                req.setAttemptId(attempt.getId());
                req.setStatus("TIMED_OUT");
                req.setTimeTakenSeconds(
                        attempt.getAllowedTimeSeconds());
                req.setRemainingTimeSeconds(0);

                submitAttempt(attempt.getUser().getId(), req);

                log.info("Auto-submitted attempt [{}] user [{}]",
                        attempt.getId(),
                        attempt.getUser().getEmail());

            } catch (Exception ex) {
                log.error("Auto-submit failed [{}]: {}",
                        attempt.getId(), ex.getMessage());
            }
        }
    }

    // ═════════════════════════════════════════════════════════════
    // WARN STUDENTS EXPIRING SOON (every 60 seconds)
    // ═════════════════════════════════════════════════════════════

    @Scheduled(fixedDelay = 60_000)
    public void warnExpiringAttempts() {
        LocalDateTime now          = LocalDateTime.now();
        LocalDateTime fiveMinLater = now.plusMinutes(5);

        List<ExamAttempt> expiringSoon = attemptRepository
                .findAttemptExpiringSoon(now, fiveMinLater);

        for (ExamAttempt attempt : expiringSoon) {
            try {
                long remaining = ChronoUnit.SECONDS.between(
                        now, attempt.getExpiresAt());

                Map<String, Object> warnPayload = new HashMap<>();
                warnPayload.put("event",     "TIME_WARNING");
                warnPayload.put("remaining", remaining);
                warnPayload.put("message",
                        "⚠️ Less than 5 minutes remaining! " +
                                "Please submit your exam.");
                warnPayload.put("attemptId", attempt.getId());

                ws.convertAndSendToUser(
                        attempt.getUser().getEmail(),
                        "/queue/timer-warning",
                        warnPayload);

            } catch (Exception ex) {
                log.warn("Failed to warn user [{}]: {}",
                        attempt.getUser().getEmail(),
                        ex.getMessage());
            }
        }
    }

    // ═════════════════════════════════════════════════════════════
    // RETRY PENDING EMAILS (every 10 minutes)
    // ═════════════════════════════════════════════════════════════

    @Scheduled(fixedDelay = 600_000)
    @Transactional
    public void retryPendingEmails() {
        List<ExamAttempt> pending =
                attemptRepository.findPendingEmailDispatch();

        pending.forEach(a -> {
            try {
                emailService.sendResultEmail(
                        a.getUser(),
                        a.getExam(),
                        toDetailedResponse(a));
                a.setResultEmailSent(true);
                attemptRepository.save(a);
                log.info("Retried result email for attempt [{}]",
                        a.getId());
            } catch (Exception ex) {
                log.error("Retry failed for attempt [{}]: {}",
                        a.getId(), ex.getMessage());
            }
        });
    }

    // ═════════════════════════════════════════════════════════════
    // RESEND RESULT EMAIL (admin trigger)
    // ═════════════════════════════════════════════════════════════

    @Transactional
    public void resendResultEmail(Long attemptId) {
        ExamAttempt attempt = attemptRepository
                .findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attempt not found: " + attemptId));

        emailService.sendResultEmail(
                attempt.getUser(),
                attempt.getExam(),
                toDetailedResponse(attempt));

        attempt.setResultEmailSent(true);
        attemptRepository.save(attempt);

        log.info("Result email resent for attempt [{}]",
                attemptId);
    }

    // ═════════════════════════════════════════════════════════════
    // GET RESULTS
    // ═════════════════════════════════════════════════════════════

    public List<AttemptResultResponse> getUserResults(
            Long userId) {
        return attemptRepository
                .findByUserIdOrderByAttemptedAtDesc(userId)
                .stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    public List<AttemptResultResponse> getExamResults(
            Long examId) {
        return attemptRepository
                .findByExamIdOrderByAttemptedAtDesc(examId)
                .stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    // ═════════════════════════════════════════════════════════════
    // STATS
    // ═════════════════════════════════════════════════════════════

    public long countTotalAttempts() {
        return attemptRepository.countTotalAttempts();
    }

    public double getOverallPassRate() {
        Double r = attemptRepository.getOverallPassRate();
        return r != null ? r : 0.0;
    }

    // ═════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═════════════════════════════════════════════════════════════

    private AttemptStartResponse buildStartResponse(
            ExamAttempt attempt,
            Exam exam,
            List<Question> questions) {

        LocalDateTime now = LocalDateTime.now();

        List<QuestionResponse> qResponses = questions.stream()
                .map(q -> questionService.mapToResponse(q, false))
                .collect(Collectors.toList());

        return AttemptStartResponse.builder()
                .attemptId(attempt.getId())
                .examId(exam.getId())
                .examTitle(exam.getTitle())
                .category(exam.getCategory())
                .difficulty(exam.getDifficulty().name())
                .instructions(exam.getInstructions())
                .durationMinutes(exam.getDurationMintues())
                .allowedTimeSeconds(attempt.getAllowedTimeSeconds())
                .startedAt(attempt.getStartedAt())
                .expiresAt(attempt.getExpiresAt())
                .serverTime(now.toString())
                .totalMarks(exam.getTotalMarks())
                .passingMarks(exam.getPassingMarks())
                .questionCount(questions.size())
                .questions(qResponses)
                .status(attempt.getStatus().name())
                .proctoringRequired(true)
                .build();
    }

    private AttemptResultResponse toSummaryResponse(
            ExamAttempt a) {
        return AttemptResultResponse.builder()
                .attemptId(a.getId())
                .examTitle(a.getExam().getTitle())
                .category(a.getExam().getCategory())
                .difficulty(a.getExam().getDifficulty().name())
                .status(a.getStatus().name())
                .allowedTimeSeconds(a.getAllowedTimeSeconds())
                .timeTakenSeconds(a.getTimeTakenSeconds())
                .remainingTimeSeconds(a.getRemainingTimeSeconds())
                .timeTakenFormatted(
                        formatTime(a.getTimeTakenSeconds()))
                .autoSubmitted(a.isAutoSubmitted())
                .scoreObtained(a.getScoreObtained())
                .totalMarks(a.getTotalMarks())
                .percentage(a.getPercentage())
                .passed(a.isPassed())
                .passingMarks(a.getExam().getPassingMarks())
                .correctAnswers(a.getCorrectAnswers())
                .wrongAnswers(a.getWrongAnswers())
                .unanswered(a.getUnanswered())
                .performanceBand(computeBand(a.getPercentage()))
                .startedAt(a.getStartedAt())
                .submittedAt(a.getSubmittedAt())
                .attemptedAt(a.getAttemptedAt())
                .build();
    }

    private AttemptResultResponse toDetailedResponse(
            ExamAttempt attempt) {

        List<AttemptAnswer> answers =
                answerRepository.findByAttemptId(attempt.getId());

        List<Question> questions = questionRepository
                .findByExamIdOrderByQuestionOrder(
                        attempt.getExam().getId());

        Map<Long, AttemptAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(
                        AttemptAnswer::getQuestionId,
                        a -> a,
                        (a, b) -> a));

        List<AttemptResultResponse.AnswerDetail> details =
                questions.stream().map(q -> {
                    AttemptAnswer aa = answerMap.get(q.getId());
                    return AttemptResultResponse.AnswerDetail
                            .builder()
                            .questionId(q.getId())
                            .questionText(q.getContent())
                            .codeSnippet(q.getCodeSnippet())
                            .language(q.getLanguage())
                            .selectedOptionId(aa != null
                                    ? aa.getSelectedOptionId()
                                    : null)
                            .selectedOptionText(aa != null
                                    ? getOptionText(q,
                                    aa.getSelectedOptionId())
                                    : null)
                            .correctOptionId(q.getCorrectOptionId())
                            .correctOptionText(getOptionText(
                                    q, q.getCorrectOptionId()))
                            .correct(aa != null && aa.isCorrect())
                            .marks(q.getMarks())
                            .explanation(q.getExplanation())
                            .build();
                }).collect(Collectors.toList());

        AttemptResultResponse r = toSummaryResponse(attempt);
        r.setAnswerDetails(details);
        return r;
    }

    private String getOptionText(Question q, Long optionId) {
        if (optionId == null || q.getOptions() == null)
            return null;
        return q.getOptions().stream()
                .filter(o -> o.getId().equals(optionId))
                .map(Option::getOptionText)
                .findFirst()
                .orElse(null);
    }

    private void saveNotification(User user,
                                  Exam exam,
                                  boolean passed,
                                  Long attemptId) {
        String msg = passed
                ? "🎉 You passed \"" + exam.getTitle() + "\"!"
                : "📋 Your result for \"" +
                exam.getTitle() + "\" is ready.";

        notificationRepository.save(Notification.builder()
                .user(user)
                .message(msg)
                .type("RESULT")
                .referenceId(attemptId)
                .read(false)
                .build());

        long unread = notificationRepository
                .countByUserIdAndReadFalse(user.getId());

        Map<String, Object> notifPayload = new HashMap<>();
        notifPayload.put("unreadCount", unread);
        notifPayload.put("latest",      msg);

        ws.convertAndSendToUser(
                user.getEmail(),
                "/queue/notifications",
                notifPayload);
    }

    // ── No ambiguity — explicit cast ──────────────────────────────
    private void sendToAdmin(Map<String, Object> payload) {
        ws.convertAndSend(
                (String) "/topic/admin/activity",
                (Object) payload);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + id));
    }

    private String computeBand(double pct) {
        if (pct >= 90) return "Excellent";
        if (pct >= 75) return "Good";
        if (pct >= 50) return "Average";
        return "Below Average";
    }

    private String formatTime(int totalSeconds) {
        int hours   = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        if (hours > 0)
            return String.format("%dh %02dm %02ds",
                    hours, minutes, seconds);
        return String.format("%dm %02ds", minutes, seconds);
    }
}