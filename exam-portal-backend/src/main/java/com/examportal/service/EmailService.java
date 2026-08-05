package com.examportal.service;

import com.examportal.dto.response.AttemptResultResponse;
import com.examportal.model.Exam;
import com.examportal.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.from-name:ExamPortal}")
    private String fromName;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // ── OTP expiry for email templates ────────────────────────────
    @Value("${app.otp.expiry-minutes:5}")
    private int expiryMinutes;

    // ═════════════════════════════════════════════════════════════
    // OTP EMAIL
    // ═════════════════════════════════════════════════════════════

    /**
     * Sends 6-digit OTP to verify email during registration.
     * Called by: OtpService.generateAndSendEmailOtp()
     * Params: to=email, name=display name or email, otp=6-digit code
     */
    @Async
    public void sendOtpEmail(String to,
                             String name,
                             String otp) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name",
                    (name == null || name.equals(to))
                            ? "User" : name);
            ctx.setVariable("otp",         otp);
            ctx.setVariable("expiryMins",  expiryMinutes);
            ctx.setVariable("frontendUrl", frontendUrl);

            String html = templateEngine.process(
                    "otp-email", ctx);

            send(to,
                    "🔐 Your ExamPortal Verification OTP",
                    html);

            log.info("OTP email sent to [{}]", to);

        } catch (Exception ex) {
            log.error("Failed to send OTP email to [{}]: {}",
                    to, ex.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════
    // PASSWORD RESET OTP EMAIL
    // ═════════════════════════════════════════════════════════════

    /**
     * Sends OTP for password reset.
     * Called by: OtpService.generateAndSendPasswordResetOtp()
     * Params: to=email, name=display name or email, otp=6-digit code
     */
    @Async
    public void sendPasswordResetOtpEmail(String to,
                                          String name,
                                          String otp) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name",
                    (name == null || name.equals(to))
                            ? "User" : name);
            ctx.setVariable("otp",         otp);
            ctx.setVariable("expiryMins",  expiryMinutes);
            ctx.setVariable("frontendUrl", frontendUrl);

            String html = templateEngine.process(
                    "otp-email", ctx);

            send(to,
                    "🔑 ExamPortal Password Reset OTP",
                    html);

            log.info("Password reset OTP email sent to [{}]", to);

        } catch (Exception ex) {
            log.error("Failed to send password reset OTP " +
                    "to [{}]: {}", to, ex.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════
    // WELCOME EMAIL
    // ═════════════════════════════════════════════════════════════

    /**
     * Sends welcome email after OTP verified.
     * Called by: AuthService.verifyEmailOtp()
     * pendingApproval=true → USER waiting for admin
     * pendingApproval=false → ADMIN auto-approved
     */
    @Async
    public void sendWelcomeEmail(String to,
                                 String name,
                                 boolean pendingApproval) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name",            name);
            ctx.setVariable("pendingApproval", pendingApproval);
            ctx.setVariable("frontendUrl",     frontendUrl);
            ctx.setVariable("loginUrl",
                    frontendUrl + "/login");

            String html = templateEngine.process(
                    "welcome-email", ctx);

            String subject = pendingApproval
                    ? "🎉 Welcome to ExamPortal — Pending Approval"
                    : "🎉 Welcome to ExamPortal — Account Active";

            send(to, subject, html);

            log.info("Welcome email sent to [{}] " +
                    "pendingApproval=[{}]", to, pendingApproval);

        } catch (Exception ex) {
            log.error("Failed to send welcome email " +
                    "to [{}]: {}", to, ex.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════
    // APPROVAL EMAIL
    // ═════════════════════════════════════════════════════════════

    /**
     * Sends email when admin approves a student account.
     * Called by: AdminService.approveUser()
     */
    @Async
    public void sendApprovalEmail(String to, String name) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name",        name);
            ctx.setVariable("frontendUrl", frontendUrl);
            ctx.setVariable("loginUrl",
                    frontendUrl + "/login");

            String html = templateEngine.process(
                    "approval-email", ctx);

            send(to,
                    "✅ ExamPortal — Account Approved!",
                    html);

            log.info("Approval email sent to [{}]", to);

        } catch (Exception ex) {
            log.error("Failed to send approval email " +
                    "to [{}]: {}", to, ex.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════
    // BLOCKED EMAIL
    // ═════════════════════════════════════════════════════════════

    /**
     * Sends email when admin blocks a student account.
     * Called by: AdminService.blockUser()
     */
    @Async
    public void sendBlockedEmail(String to,
                                 String name,
                                 String reason) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name",   name);
            ctx.setVariable("reason",
                    reason != null ? reason
                            : "Violation of platform policy.");
            ctx.setVariable("frontendUrl", frontendUrl);

            String html = templateEngine.process(
                    "blocked-email", ctx);

            send(to,
                    "🚫 ExamPortal — Account Blocked",
                    html);

            log.info("Blocked email sent to [{}]", to);

        } catch (Exception ex) {
            log.error("Failed to send blocked email " +
                    "to [{}]: {}", to, ex.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════
    // UNBLOCK EMAIL
    // ═════════════════════════════════════════════════════════════

    /**
     * Sends email when admin unblocks a student account.
     * Called by: AdminService.unblockUser()
     */
    @Async
    public void sendUnblockedEmail(String to, String name) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name",        name);
            ctx.setVariable("frontendUrl", frontendUrl);
            ctx.setVariable("loginUrl",
                    frontendUrl + "/login");

            String html = templateEngine.process(
                    "approval-email", ctx);

            send(to,
                    "🔓 ExamPortal — Account Unblocked",
                    html);

            log.info("Unblocked email sent to [{}]", to);

        } catch (Exception ex) {
            log.error("Failed to send unblocked email " +
                    "to [{}]: {}", to, ex.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════
    // RESULT EMAIL
    // ═════════════════════════════════════════════════════════════

    /**
     * Sends exam result email after submission.
     * Called by: ExamAttemptService.submitAttempt()
     * Called by: ExamAttemptService.resendResultEmail()
     * Called by: ExamAttemptService.retryPendingEmails()
     */
    @Async
    public void sendResultEmail(User user,
                                Exam exam,
                                AttemptResultResponse result) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name",
                    user.getName());
            ctx.setVariable("examTitle",
                    exam.getTitle());
            ctx.setVariable("category",
                    exam.getCategory());
            ctx.setVariable("difficulty",
                    exam.getDifficulty().name());
            ctx.setVariable("score",
                    result.getScoreObtained());
            ctx.setVariable("totalMarks",
                    result.getTotalMarks());
            ctx.setVariable("percentage",
                    result.getPercentage());
            ctx.setVariable("passed",
                    result.isPassed());
            ctx.setVariable("passingMarks",
                    result.getPassingMarks());
            ctx.setVariable("correctAnswers",
                    result.getCorrectAnswers());
            ctx.setVariable("wrongAnswers",
                    result.getWrongAnswers());
            ctx.setVariable("unanswered",
                    result.getUnanswered());
            ctx.setVariable("timeTaken",
                    result.getTimeTakenFormatted());
            ctx.setVariable("performanceBand",
                    result.getPerformanceBand());
            ctx.setVariable("autoSubmitted",
                    result.isAutoSubmitted());
            ctx.setVariable("status",
                    result.getStatus());
            ctx.setVariable("frontendUrl",
                    frontendUrl);
            ctx.setVariable("resultsUrl",
                    frontendUrl + "/results/" +
                            result.getAttemptId());

            String html = templateEngine.process(
                    "result-email", ctx);

            String subject = result.isPassed()
                    ? "🎉 You Passed — " + exam.getTitle()
                    : "📋 Exam Result — " + exam.getTitle();

            send(user.getEmail(), subject, html);

            log.info("Result email sent to [{}] passed=[{}]",
                    user.getEmail(), result.isPassed());

        } catch (Exception ex) {
            log.error("Failed to send result email " +
                            "to [{}]: {}", user.getEmail(),
                    ex.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════
    // TIMED-OUT EMAIL
    // ═════════════════════════════════════════════════════════════

    /**
     * Sends auto-submit notice when exam time expires.
     * Called by: ExamAttemptService.submitAttempt()
     *            when isTimedOut = true
     */
    @Async
    public void sendTimedOutEmail(String to,
                                  String name,
                                  String examTitle,
                                  AttemptResultResponse result) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name",      name);
            ctx.setVariable("examTitle", examTitle);
            ctx.setVariable("score",
                    result.getScoreObtained());
            ctx.setVariable("totalMarks",
                    result.getTotalMarks());
            ctx.setVariable("percentage",
                    result.getPercentage());
            ctx.setVariable("passed",
                    result.isPassed());
            ctx.setVariable("timeTaken",
                    result.getTimeTakenFormatted());
            ctx.setVariable("correct",
                    result.getCorrectAnswers());
            ctx.setVariable("wrong",
                    result.getWrongAnswers());
            ctx.setVariable("unanswered",
                    result.getUnanswered());
            ctx.setVariable("frontendUrl", frontendUrl);

            String html = templateEngine.process(
                    "timed-out-email", ctx);

            send(to,
                    "⏰ Exam Auto-Submitted — " + examTitle,
                    html);

            log.info("Timed-out email sent to [{}]", to);

        } catch (Exception ex) {
            log.error("Failed to send timed-out email " +
                    "to [{}]: {}", to, ex.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════
    // DISQUALIFICATION EMAIL
    // ═════════════════════════════════════════════════════════════

    /**
     * Sends disqualification notice when proctoring
     * detects fraudulent activity.
     * Called by: ProctoringService.disqualifyStudent()
     */
    @Async
    public void sendDisqualificationEmail(String to,
                                          String name,
                                          String reason) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name",   name);
            ctx.setVariable("reason",
                    reason != null ? reason
                            : "Fraudulent activity detected " +
                            "during exam.");
            ctx.setVariable("frontendUrl", frontendUrl);

            String html = templateEngine.process(
                    "disqualification-email", ctx);

            send(to,
                    "🚫 Exam Disqualification Notice — ExamPortal",
                    html);

            log.info("Disqualification email sent to [{}]", to);

        } catch (Exception ex) {
            log.error("Failed to send disqualification email " +
                    "to [{}]: {}", to, ex.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════
    // PROCTORING ALERT EMAIL (sent to admin)
    // ═════════════════════════════════════════════════════════════

    /**
     * Sends proctoring alert to admin when student is
     * flagged or disqualified.
     * Called by: ProctoringService.notifyAdminOfPendingAlerts()
     */
    @Async
    public void sendProctoringAlertEmail(String studentEmail,
                                         String studentName,
                                         boolean disqualified,
                                         int totalViolations,
                                         String reason) {
        try {
            Context ctx = new Context();
            ctx.setVariable("studentName",     studentName);
            ctx.setVariable("studentEmail",    studentEmail);
            ctx.setVariable("disqualified",    disqualified);
            ctx.setVariable("totalViolations", totalViolations);
            ctx.setVariable("reason",
                    reason != null ? reason : "N/A");
            ctx.setVariable("status",
                    disqualified ? "DISQUALIFIED" : "FLAGGED");
            ctx.setVariable("frontendUrl",  frontendUrl);
            ctx.setVariable("adminUrl",
                    frontendUrl + "/admin/proctoring");

            String html = templateEngine.process(
                    "proctoring-alert-email", ctx);

            String subject = disqualified
                    ? "🚨 Student Disqualified — ExamPortal"
                    : "⚠️ Student Flagged — ExamPortal";

            // ── Send to admin email ───────────────────────────────
            send(fromEmail, subject, html);

            log.info("Proctoring alert sent to admin " +
                            "for student [{}] disqualified=[{}]",
                    studentEmail, disqualified);

        } catch (Exception ex) {
            log.error("Failed to send proctoring alert: {}",
                    ex.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════
    // AI FEEDBACK EMAIL
    // ═════════════════════════════════════════════════════════════

    /**
     * Sends AI-generated exam feedback after result.
     * Called by: AiFeedbackService.generateAndSaveExamFeedback()
     */
    @Async
    public void sendAiFeedbackEmail(String to,
                                    String name,
                                    String examTitle,
                                    String feedbackContent,
                                    String studySuggestions) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name",             name);
            ctx.setVariable("examTitle",        examTitle);
            ctx.setVariable("feedback",         feedbackContent);
            ctx.setVariable("studySuggestions", studySuggestions);
            ctx.setVariable("frontendUrl",      frontendUrl);

            String html = templateEngine.process(
                    "ai-feedback-email", ctx);

            send(to,
                    "🤖 Your AI Study Feedback — " + examTitle,
                    html);

            log.info("AI feedback email sent to [{}]", to);

        } catch (Exception ex) {
            log.error("Failed to send AI feedback email " +
                    "to [{}]: {}", to, ex.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════
    // CORE SEND METHOD
    // ═════════════════════════════════════════════════════════════

    /**
     * Core method — builds MimeMessage and sends via SMTP.
     * All public email methods call this.
     */
    private void send(String to,
                      String subject,
                      String htmlBody) {
        try {
            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML
            helper.setFrom(fromEmail, fromName);

            mailSender.send(message);

            log.debug("Email sent to [{}] subject=[{}]",
                    to, subject);

        } catch (MessagingException ex) {
            log.error("MessagingException to [{}]: {}",
                    to, ex.getMessage());
            throw new RuntimeException(
                    "Failed to send email to: " + to, ex);

        } catch (Exception ex) {
            log.error("Unexpected error to [{}]: {}",
                    to, ex.getMessage());
            throw new RuntimeException(
                    "Failed to send email to: " + to, ex);
        }
    }
}