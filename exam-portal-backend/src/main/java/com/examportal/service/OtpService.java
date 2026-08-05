package com.examportal.service;

import com.examportal.model.OtpVerification;
import com.examportal.repository.OtpRepository;
import com.examportal.util.OtpUtil;
import com.examportal.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository  otpRepository;
    private final OtpUtil        otpUtil;
    private final EmailService   emailService;

    @Value("${app.otp.expiry-minutes:5}")
    private int expiryMinutes;

    private static final int MAX_ATTEMPTS = 3;

    // ── Generate & send email OTP ─────────────────────────────────
    @Transactional
    public void generateAndSendEmailOtp(String email) {

        otpRepository.deleteIdentifierAndType(
                email, OtpVerification.OtpType.EMAIL);

        String code = otpUtil.generateNumericOtp();

        OtpVerification otp = OtpVerification.builder()
                .identifier(email)
                .otpCode(code)
                .type(OtpVerification.OtpType.EMAIL)
                .expiresAt(LocalDateTime.now()
                        .plusMinutes(expiryMinutes))
                .used(false)
                .attemptCount(0)
                .build();

        otpRepository.save(otp);
        emailService.sendOtpEmail(email, email, code);
        log.info("OTP generated & sent to [{}]", email);
    }

    // ── Generate & send password reset OTP ───────────────────────
    @Transactional
    public void generateAndSendPasswordResetOtp(String email) {

        otpRepository.deleteIdentifierAndType(
                email, OtpVerification.OtpType.PASSWORD_RESET);

        String code = otpUtil.generateNumericOtp();

        OtpVerification otp = OtpVerification.builder()
                .identifier(email)
                .otpCode(code)
                .type(OtpVerification.OtpType.PASSWORD_RESET)
                .expiresAt(LocalDateTime.now()
                        .plusMinutes(expiryMinutes))
                .used(false)
                .attemptCount(0)
                .build();

        otpRepository.save(otp);

        emailService.sendPasswordResetOtpEmail(email, email, code);

        log.info("Password reset OTP sent to [{}]", email);
    }

    // ── Verify OTP ────────────────────────────────────────────────
    @Transactional
    public boolean verifyOtp(String identifier,
                             String code,
                             OtpVerification.OtpType type) {

        Optional<OtpVerification> otpOpt =
                otpRepository
                        .findTopByIdentifierAndTypeUsedFalseOrderByCreatedAtDesc(
                                identifier, type);

        if (otpOpt.isEmpty())
            throw new ValidationException(
                    "No OTP found. Please request a new one.");

        OtpVerification otp = otpOpt.get();

        // ── Increment attempt count ───────────────────────────────
        otp.setAttemptCount(otp.getAttemptCount() + 1);
        otpRepository.save(otp);

        // ── Check max attempts ────────────────────────────────────
        if (otp.getAttemptCount() > MAX_ATTEMPTS) {
            otp.setUsed(true);
            otpRepository.save(otp);
            throw new ValidationException(
                    "Too many failed attempts. " +
                            "Please request a new OTP.");
        }

        // ── Check expiry ──────────────────────────────────────────
        if (otp.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new ValidationException(
                    "OTP has expired. Please request a new one.");

        // ── Check code match ──────────────────────────────────────
        if (!otp.getOtpCode().equals(code)) {
            int remaining = MAX_ATTEMPTS - otp.getAttemptCount();
            throw new ValidationException(
                    "Invalid OTP. " + remaining +
                            " attempt(s) remaining.");
        }

        // ── Mark used ─────────────────────────────────────────────
        otp.setUsed(true);
        otpRepository.save(otp);

        log.info("OTP verified successfully for [{}]", identifier);
        return true;
    }

    // ── Cleanup expired OTPs (every hour) ────────────────────────
    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteAllExpiredBefore(LocalDateTime.now());
        log.info("Expired OTPs cleaned up.");
    }
}