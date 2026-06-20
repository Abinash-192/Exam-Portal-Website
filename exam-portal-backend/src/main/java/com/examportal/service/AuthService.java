//package com.examportal.service;
//
//import com.examportal.dto.request.LoginRequest;
//import com.examportal.dto.request.RegisterRequest;
//import com.examportal.dto.response.AuthResponse;
//import com.examportal.exception.ResourceNotFoundException;
//import com.examportal.exception.ValidationException;
//import com.examportal.model.OtpVerification;
//import com.examportal.model.Role;
//import com.examportal.model.User;
//import com.examportal.repository.UserRepository;
//import com.examportal.security.JwtTokenProvider;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.DisabledException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtTokenProvider tokenProvider;
//    private final AuthenticationManager authManager;
//    private final OtpService otpService;
//    private final UserService userService;
//    private final EmailService emailService;
//
//    //Register
//    @Transactional
//    public String register(RegisterRequest req){
//
//        if (userRepository.existsByEmail(req.getEmail())) {
//            throw new ValidationException("Email is already registered.");
//        }
//        if (userRepository.existsByMobile(req.getMobile())) {
//            throw new ValidationException("Mobile number is already registered.");
//        }
//
//        Role role = "ADMIN".equalsIgnoreCase(req.getRole()) ? Role.ADMIN :Role.USER;
//
//        User user = User.builder()
//                .name(req.getName())
//                .email(req.getEmail())
//                .mobile(req.getMobile())
//                .password(passwordEncoder.encode(req.getPassword()))
//                .role(role)
//                .provider("local")
//                .enabled(false)
//                .emailVerified(false)
//                .approved(false)
//                .blocked(false)
//                .build();
//
//        userRepository.save(user);
//
//        otpService.generateAndSendEmailOtp(req.getEmail());
//        log.info("User registered : [{}] with role [{}]", req.getEmail(), role);
//        return  "Registration successful. Please check your email for a 6 digit OTP.";
//    }
//
//    //Verify email otp
//    @Transactional
//    public String verifyEmailOtp(String email, String otp){
//
//        otpService.verifyOtp(email, otp, OtpVerification.OtpType.EMAIL);
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
//
//        user.setEmailVerified(true);
//        if (user.getRole() == Role.ADMIN) {
//
//            user.setApproved(true);
//            user.setEnabled(true);
//        }
//
//        userRepository.save(user);
//        emailService.sendWelcomeEmail(user.getEmail(), user.getName(),
//                user.getRole() == Role.USER);
//
//        String message = user.getRole() == Role.ADMIN
//                ? "Email verified! Your admin account is active now."
//                : "Email verified! Your account is pending admin approval.";
//
//        log.info("Email verified for [{}]", email);
//        return message;
//    }
//
//    //Login
//    public AuthResponse login(LoginRequest req){
//
//        User user = userRepository.findByEmail(req.getEmail())
//                .orElseThrow(() -> new ValidationException("Invalid email or password."));
//
//        if (!user.isEmailVerified()) {
//
//            throw new ValidationException("Please verify your email before logging in.");
//        }
//        if (user.isBlocked()) {
//
//            throw new ValidationException("Your account has been blocked.Please contact the administrator");
//        }
//        if (user.getRole() == Role.USER && !user.isApproved()) {
//
//            throw new ValidationException("Your account is pending admin approval. You will be notified via email.");
//        }
//
//        try{
//
//            Authentication authentication = authManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
//
//              String token = tokenProvider.generateToken(authentication);
//              String refreshToken = tokenProvider.generateRefreshToken(req.getEmail());
//
//              log.info("User [{}] logged in successfully.", req.getEmail());
//
//              return AuthResponse.builder()
//                      .token(token)
//                      .refreshToken("Bearer")
//                      .user(userRepository.mapToRespose(user))
//                      .build();
//
//        } catch (BadCredentialsException e) {
//            throw new ValidationException("Invalid email or password.");
//        } catch (DisabledException e) {
//            throw new ValidationException("Account is disabled");
//        }
//    }
//
//    //Resend otp
//    @Transactional
//    public String resendOtp(String email){
//
//        if (!userRepository.existsByEmail(email)) {
//
//            throw new ResourceNotFoundException("No account found with email:"+ email);
//
//            otpService.generateAndSendEmailOtp(email);
//
//        }
//        return "A new OTP has been sent to "+ email;
//    }
//
//    //Refresh token
//    public AuthResponse refreshToken(String refreshToken){
//
//        if (!tokenProvider.validateToken(refreshToken)) {
//
//            throw new ValidationException("Invalid or expired refresh token.");
//
//            String email = tokenProvider.getEmailFromToken(refreshToken);
//            User user = userRepository.findByEmail(email)
//                    .orElseThrow(() -> new ResourceNotFoundException("User not found."));
//
//            String newToken = tokenProvider.generateTokenFromEmail(email);
//
//        }
//        return AuthResponse.builder()
//                .token(newToken)
//                .tokenType("Bearer")
//                .user(userService.mapToResponse(user))
//                .build();
//    }
//
//    //Forgot password send reset otp
//    @Transactional
//    public String forgotPassword(String email){
//
//        if (!userRepository.existsByEmail(email)) {
//
//            throw new ResourceNotFoundException("No account with that email.");
//        }
//        otpService.generateAndSendPasswordResetOtp(email);
//        return "Password rest OTP sent to "+email;
//    }
//
//    //Reset password
//    @Transactional
//    public String resetPassword(String email, String otp, String newpassword) {
//
//        otpService.verifyOtp(email, otp, OtpVerification.OtpType.PASSWORD_RESET);
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
//
//        user.setPassword(passwordEncoder.encode(newpassword));
//        userRepository.save(user);
//        log.info("Password reset for [{}]",email);
//        return  "Password updated successfully.";
//    }
//
//}


package com.examportal.service;

import com.examportal.dto.request.LoginRequest;
import com.examportal.dto.request.RegisterRequest;
import com.examportal.dto.response.AuthResponse;
import com.examportal.exception.ResourceNotFoundException;
import com.examportal.exception.ValidationException;
import com.examportal.model.OtpVerification;
import com.examportal.model.Role;
import com.examportal.model.User;
import com.examportal.repository.UserRepository;
import com.examportal.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtTokenProvider      tokenProvider;
    private final AuthenticationManager authManager;
    private final OtpService            otpService;
    private final UserService           userService;
    private final EmailService          emailService;

    // ─────────────────────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public String register(RegisterRequest req) {

        // ── Duplicate checks ──────────────────────────────────────
        if (userRepository.existsByEmail(req.getEmail()))
            throw new ValidationException(
                    "Email [" + req.getEmail() + "] is already registered.");

        if (userRepository.existsByMobile(req.getMobile()))
            throw new ValidationException(
                    "Mobile number [" + req.getMobile() + "] is already registered.");

        // ── Resolve role ──────────────────────────────────────────
        Role role = "ADMIN".equalsIgnoreCase(req.getRole())
                ? Role.ADMIN
                : Role.USER;

        // ── Build & persist user ──────────────────────────────────
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .mobile(req.getMobile())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .provider("local")
                .enabled(false)        // enabled after email OTP verified
                .emailVerified(false)
                .mobileVerified(false)
                .approved(false)       // admin must approve USER accounts
                .blocked(false)
                .build();

        userRepository.save(user);

        // ── Send email OTP ────────────────────────────────────────
        otpService.generateAndSendEmailOtp(req.getEmail());

        log.info("User registered: [{}] role [{}]. OTP sent.",
                req.getEmail(), role);

        return "Registration successful! " +
                "Please check your email for a 6-digit OTP to verify your account.";
    }

    // ─────────────────────────────────────────────────────────────
    // VERIFY EMAIL OTP
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public String verifyEmailOtp(String email, String otp) {

        // ── Validate OTP ──────────────────────────────────────────
        otpService.verifyOtp(email, otp, OtpVerification.OtpType.EMAIL);

        // ── Load user ─────────────────────────────────────────────
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No user found with email: " + email));

        // ── Mark email as verified ────────────────────────────────
        user.setEmailVerified(true);

        // ── ADMIN accounts are auto-approved & enabled ────────────
        // ── USER accounts wait for admin approval ─────────────────
        if (user.getRole() == Role.ADMIN) {
            user.setApproved(true);
            user.setEnabled(true);
        }

        userRepository.save(user);

        // ── Send welcome email ────────────────────────────────────
        emailService.sendWelcomeEmail(
                user.getEmail(),
                user.getName(),
                user.getRole() == Role.USER   // pendingApproval flag
        );

        String message = user.getRole() == Role.ADMIN
                ? "Email verified! Your admin account is now active."
                : "Email verified! Your account is pending admin approval. " +
                "You will be notified via email once approved.";

        log.info("Email verified for [{}] role [{}]",
                email, user.getRole());

        return message;
    }

    // ─────────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest req) {

        // ── Pre-authentication guards ─────────────────────────────
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ValidationException(
                        "Invalid email or password."));

        if (!user.isEmailVerified())
            throw new ValidationException(
                    "Your email is not verified. " +
                            "Please verify your email before logging in.");

        if (user.isBlocked())
            throw new ValidationException(
                    "Your account has been blocked by the administrator. " +
                            "Please contact support.");

        if (user.getRole() == Role.USER && !user.isApproved())
            throw new ValidationException(
                    "Your account is pending admin approval. " +
                            "You will be notified via email once approved.");

        // ── Authenticate via Spring Security ──────────────────────
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            req.getEmail(),
                            req.getPassword()));

            // ── Generate JWT tokens ───────────────────────────────
            String accessToken  = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(req.getEmail());

            // ── Update last login timestamp ───────────────────────
            userService.updateLastLogin(req.getEmail());

            log.info("User [{}] logged in successfully.", req.getEmail());

            return AuthResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .user(userService.mapToResponse(user))
                    .build();

        } catch (BadCredentialsException e) {
            throw new ValidationException("Invalid email or password.");

        } catch (DisabledException e) {
            throw new ValidationException(
                    "Your account is disabled. Please contact support.");

        } catch (LockedException e) {
            throw new ValidationException(
                    "Your account is locked. Please contact the administrator.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // RESEND OTP
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public String resendOtp(String email) {
        if (!userRepository.existsByEmail(email))
            throw new ResourceNotFoundException(
                    "No account found with email: " + email);

        otpService.generateAndSendEmailOtp(email);

        log.info("OTP resent to [{}]", email);
        return "A new OTP has been sent to " + email;
    }

    // ─────────────────────────────────────────────────────────────
    // REFRESH TOKEN
    // ─────────────────────────────────────────────────────────────

    public AuthResponse refreshToken(String refreshToken) {

        if (!tokenProvider.validateToken(refreshToken))
            throw new ValidationException(
                    "Invalid or expired refresh token. Please log in again.");

        String email = tokenProvider.getEmailFromToken(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for token: " + email));

        // ── Guard: blocked users cannot refresh ───────────────────
        if (user.isBlocked())
            throw new ValidationException(
                    "Your account has been blocked.");

        String newAccessToken = tokenProvider.generateTokenFromEmail(email);

        log.info("Token refreshed for [{}]", email);

        return AuthResponse.builder()
                .token(newAccessToken)
                .tokenType("Bearer")
                .user(userService.mapToResponse(user))
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // FORGOT PASSWORD — send reset OTP
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public String forgotPassword(String email) {
        if (!userRepository.existsByEmail(email))
            throw new ResourceNotFoundException(
                    "No account found with email: " + email);

        otpService.generateAndSendPasswordResetOtp(email);

        log.info("Password reset OTP sent to [{}]", email);
        return "A password-reset OTP has been sent to " + email;
    }

    // ─────────────────────────────────────────────────────────────
    // RESET PASSWORD — verify OTP + set new password
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public String resetPassword(String email,
                                String otp,
                                String newPassword) {

        // ── Validate reset OTP ────────────────────────────────────
        otpService.verifyOtp(email, otp,
                OtpVerification.OtpType.PASSWORD_RESET);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));

        // ── Prevent reuse of old password ─────────────────────────
        if (passwordEncoder.matches(newPassword, user.getPassword()))
            throw new ValidationException(
                    "New password must be different from your current password.");

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password reset successfully for [{}]", email);
        return "Password updated successfully. You can now log in.";
    }

    // ─────────────────────────────────────────────────────────────
    // OAUTH2 POST-LOGIN — called from OAuth2SuccessHandler
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse handleOAuth2Login(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "OAuth2 user not found: " + email));

        if (user.isBlocked())
            throw new ValidationException(
                    "Your account has been blocked.");

        if (user.getRole() == Role.USER && !user.isApproved())
            throw new ValidationException(
                    "Your account is pending admin approval.");

        String accessToken  = tokenProvider.generateTokenFromEmail(email);
        String refreshToken = tokenProvider.generateRefreshToken(email);

        // ── Update last login timestamp ───────────────────────────
        userService.updateLastLogin(email);

        log.info("OAuth2 login handled for [{}]", email);

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(userService.mapToResponse(user))
                .build();
    }
}