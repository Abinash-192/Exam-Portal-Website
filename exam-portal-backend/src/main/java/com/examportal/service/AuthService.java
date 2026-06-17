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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authManager;
    private final OtpService otpService;
    private final UserService userService;
    private final EmailService emailService;

    //Register
    @Transactional
    public String register(RegisterRequest req){

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ValidationException("Email is already registered.");
        }
        if (userRepository.existsByMobile(req.getMobile())) {
            throw new ValidationException("Mobile number is already registered.");
        }

        Role role = "ADMIN".equalsIgnoreCase(req.getRole()) ? Role.ADMIN :Role.USER;

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .mobile(req.getMobile())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .provider("local")
                .enabled(false)
                .emailVerified(false)
                .approved(false)
                .blocked(false)
                .build();

        userRepository.save(user);

        otpService.generateAndSendEmailOtp(req.getEmail());
        log.info("User registered : [{}] with role [{}]", req.getEmail(), role);
        return  "Registration successful. Please check your email for a 6 digit OTP.";
    }

    //Verify email otp
    @Transactional
    public String verifyEmailOtp(String email, String otp){

        otpService.verifyOtp(email, otp, OtpVerification.OtpType.EMAIL);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        user.setEmailVerified(true);
        if (user.getRole() == Role.ADMIN) {

            user.setApproved(true);
            user.setEnabled(true);
        }

        userRepository.save(user);
        emailService.sendWelcomeEmail(user.getEmail(), user.getName(),
                user.getRole() == Role.USER);

        String message = user.getRole() == Role.ADMIN
                ? "Email verified! Your admin account is active now."
                : "Email verified! Your account is pending admin approval.";

        log.info("Email verified for [{}]", email);
        return message;
    }

    //Login
    public AuthResponse login(LoginRequest req){

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ValidationException("Invalid email or password."));

        if (!user.isEmailVerified()) {

            throw new ValidationException("Please verify your email before logging in.");
        }
        if (user.isBlocked()) {

            throw new ValidationException("Your account has been blocked.Please contact the administrator");
        }
        if (user.getRole() == Role.USER && !user.isApproved()) {

            throw new ValidationException("Your account is pending admin approval. You will be notified via email.");
        }

        try{

            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

              String token = tokenProvider.generateToken(authentication);
              String refreshToken = tokenProvider.generateRefreshToken(req.getEmail());

              log.info("User [{}] logged in successfully.", req.getEmail());

              return AuthResponse.builder()
                      .token(token)
                      .refreshToken("Bearer")
                      .user(userRepository.mapToRespose(user))
                      .build();

        } catch (BadCredentialsException e) {
            throw new ValidationException("Invalid email or password.");
        } catch (DisabledException e) {
            throw new ValidationException("Account is disabled");
        }
    }

    //Resend otp
    @Transactional
    public String resendOtp(String email){

        if (!userRepository.existsByEmail(email)) {

            throw new ResourceNotFoundException("No account found with email:"+ email);

            otpService.generateAndSendEmailOtp(email);

        }
        return "A new OTP has been sent to "+ email;
    }

    //Refresh token
    public AuthResponse refreshToken(String refreshToken){

        if (!tokenProvider.validateToken(refreshToken)) {

            throw new ValidationException("Invalid or expired refresh token.");

            String email = tokenProvider.getEmailFromToken(refreshToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found."));

            String newToken = tokenProvider.generateTokenFromEmail(email);

        }
        return AuthResponse.builder()
                .token(newToken)
                .tokenType("Bearer")
                .user(userService.mapToResponse(user))
                .build();
    }

    //Forgot password send reset otp
    @Transactional
    public String forgotPassword(String email){

        if (!userRepository.existsByEmail(email)) {

            throw new ResourceNotFoundException("No account with that email.");
        }
        otpService.generateAndSendPasswordResetOtp(email);
        return "Password rest OTP sent to "+email;
    }

    //Reset password
    @Transactional
    public String resetPassword(String email, String otp, String newpassword) {

        otpService.verifyOtp(email, otp, OtpVerification.OtpType.PASSWORD_RESET);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        user.setPassword(passwordEncoder.encode(newpassword));
        userRepository.save(user);
        log.info("Password reset for [{}]",email);
        return  "Password updated successfully.";
    }

}
