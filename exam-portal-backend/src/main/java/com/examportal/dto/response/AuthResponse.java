package com.examportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "success",
        "message",
        "accessToken",
        "refreshToken",
        "tokenType",
        "expiresIn",
        "user",
        "requiresOtp",
        "otpSentTo",
        "requiresApproval",
        "provider"
})
public class AuthResponse {

    // ── Status ────────────────────────────────────────────────────
    private boolean success;
    private String  message;

    // ── Tokens ────────────────────────────────────────────────────
    // null during OTP step — set after OTP verified
    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    // Token expiry in milliseconds (86400000 = 24h)
    private Long expiresIn;

    // ── User info ─────────────────────────────────────────────────
    // null during OTP step — set after OTP verified / login
    private UserInfo user;

    // ── OTP flow flags ────────────────────────────────────────────
    // true = OTP sent, client must call /verify-otp next
    private boolean requiresOtp;

    // Email or masked mobile where OTP was sent
    // e.g. "j***@gmail.com" or "98*****210"
    private String otpSentTo;

    // ── Admin approval ────────────────────────────────────────────
    // true = registered but admin hasn't approved yet
    private boolean requiresApproval;

    // ── OAuth2 ────────────────────────────────────────────────────
    // local | google | github
    private String provider;

    // ── Timestamp ─────────────────────────────────────────────────
    @Builder.Default
    private String timestamp = Instant.now().toString();

    // ═════════════════════════════════════════════════════════════
    // FACTORIES
    // ═════════════════════════════════════════════════════════════

    // ── Registration success — OTP sent ───────────────────────────
    public static AuthResponse registered(String otpSentTo) {
        return AuthResponse.builder()
                .success(true)
                .message("Registration successful. " +
                        "Please verify your email OTP.")
                .requiresOtp(true)
                .requiresApproval(false)
                .otpSentTo(otpSentTo)
                .timestamp(Instant.now().toString())
                .build();
    }

    // ── OTP verified — pending admin approval ─────────────────────
    public static AuthResponse pendingApproval(String email) {
        return AuthResponse.builder()
                .success(true)
                .message("Email verified successfully. " +
                        "Your account is pending admin approval. " +
                        "You will be notified once approved.")
                .requiresOtp(false)
                .requiresApproval(true)
                .otpSentTo(email)
                .timestamp(Instant.now().toString())
                .build();
    }

    // ── Login success — tokens issued ─────────────────────────────
    public static AuthResponse loginSuccess(String accessToken,
                                            String refreshToken,
                                            Long expiresIn,
                                            UserInfo user,
                                            String provider) {
        return AuthResponse.builder()
                .success(true)
                .message("Login successful.")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(user)
                .requiresOtp(false)
                .requiresApproval(false)
                .provider(provider)
                .timestamp(Instant.now().toString())
                .build();
    }

    // ── Token refreshed ───────────────────────────────────────────
    public static AuthResponse tokenRefreshed(String accessToken,
                                              String refreshToken,
                                              Long expiresIn) {
        return AuthResponse.builder()
                .success(true)
                .message("Token refreshed successfully.")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .timestamp(Instant.now().toString())
                .build();
    }

    // ── OTP resent ────────────────────────────────────────────────
    public static AuthResponse otpResent(String otpSentTo) {
        return AuthResponse.builder()
                .success(true)
                .message("OTP resent successfully.")
                .requiresOtp(true)
                .otpSentTo(otpSentTo)
                .timestamp(Instant.now().toString())
                .build();
    }

    // ── Password reset OTP sent ───────────────────────────────────
    public static AuthResponse passwordResetOtpSent(
            String otpSentTo) {
        return AuthResponse.builder()
                .success(true)
                .message("Password reset OTP sent to your email.")
                .requiresOtp(true)
                .otpSentTo(otpSentTo)
                .timestamp(Instant.now().toString())
                .build();
    }

    // ── Password reset success ────────────────────────────────────
    public static AuthResponse passwordResetSuccess() {
        return AuthResponse.builder()
                .success(true)
                .message("Password reset successfully. " +
                        "You can now login with your new password.")
                .timestamp(Instant.now().toString())
                .build();
    }

    // ── OAuth2 login / register success ──────────────────────────
    public static AuthResponse oauth2Success(String accessToken,
                                             String refreshToken,
                                             Long expiresIn,
                                             UserInfo user,
                                             String provider) {
        return AuthResponse.builder()
                .success(true)
                .message("OAuth2 login successful via " + provider)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(user)
                .requiresOtp(false)
                .requiresApproval(false)
                .provider(provider)
                .timestamp(Instant.now().toString())
                .build();
    }

    // ── OAuth2 registered — pending approval ──────────────────────
    public static AuthResponse oauth2PendingApproval(
            String provider, String email) {
        return AuthResponse.builder()
                .success(true)
                .message("Account created via " + provider +
                        ". Pending admin approval.")
                .requiresOtp(false)
                .requiresApproval(true)
                .provider(provider)
                .otpSentTo(email)
                .timestamp(Instant.now().toString())
                .build();
    }

    // ── Error ─────────────────────────────────────────────────────
    public static AuthResponse error(String message) {
        return AuthResponse.builder()
                .success(false)
                .message(message)
                .timestamp(Instant.now().toString())
                .build();
    }

    // ═════════════════════════════════════════════════════════════
    // NESTED: UserInfo
    // ═════════════════════════════════════════════════════════════

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {

        private Long    id;
        private String  name;
        private String  email;
        private String  mobile;
        private String  role;      // ADMIN | USER
        private String  provider;  // local | google | GitHub
        private String  profilePicture;
        private boolean approved;
        private boolean blocked;
        private boolean emailVerified;
    }
}