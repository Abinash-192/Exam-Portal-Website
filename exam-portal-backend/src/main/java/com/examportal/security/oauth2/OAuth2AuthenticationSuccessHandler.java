//package com.examportal.security.oauth2;
//
//import com.examportal.model.User;
//import com.examportal.repository.UserRepository;
//import com.examportal.security.JwtTokenProvider;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.io.IOException;
//
//@Component
//@Slf4j
//
//public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//
//    private final JwtTokenProvider tokenProvider;
//    private final UserRepository userRepository;
//
//    @Value("${app.frontend-url}")
//     private String frontendUrl;
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
////        super.onAuthenticationSuccess(request, response, authentication);
//        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//        String email = oAuth2User.getAttribute("email");
//
//        User user = userRepository.findByEmail(email).orElse(null);
//        if (user == null) {
//            getRedirectStrategy().sendRedirect(request,response,frontendUrl + "/login?error = user not found");
//            return;
//        }
//
//        String token = tokenProvider.generateTokenFromEmail(email);
//        String refreshToken = tokenProvider.generateRefreshToken(email);
//
//        String targetUrl = UriComponentsBuilder
//                .fromUriString(frontendUrl + "/oauth2/callback")
//                .queryParam("token", token)
//                .queryParam("refreshToken",refreshToken)
//                .queryParam("role",user.getRole().name())
//                .build().toUriString();
//
//        logger.info("OAuth2 login success for [{}], redirecting ",email);
//        getRedirectStrategy().sendRedirect(request,response,targetUrl);
//
//    }
//}


package com.examportal.security.oauth2;

import com.examportal.model.Role;
import com.examportal.model.User;
import com.examportal.repository.UserRepository;
import com.examportal.security.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository   userRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

//    public OAuth2AuthenticationSuccessHandler(
//            JwtTokenProvider tokenProvider,
//            UserRepository userRepository) {
//        this.tokenProvider  = tokenProvider;
//        this.userRepository = userRepository;
//    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User =
                (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");

        // ── User not found → redirect to login with error ─────────
        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            getRedirectStrategy().sendRedirect(
                    request,
                    response,
                    frontendUrl + "/login?error=user_not_found");
            return;
        }

        // ── Blocked user → redirect with error ───────────────────
        if (user.isBlocked()) {
            getRedirectStrategy().sendRedirect(
                    request,
                    response,
                    frontendUrl + "/login?error=account_blocked");
            return;
        }

        // ── Pending approval → redirect with message ──────────────
        if (user.getRole() == Role.USER && !user.isApproved()) {
            getRedirectStrategy().sendRedirect(
                    request,
                    response,
                    frontendUrl +
                            "/login?error=pending_approval");
            return;
        }

        // ── Generate tokens ───────────────────────────────────────
        String token        = tokenProvider
                .generateTokenFromEmail(email);
        String refreshToken = tokenProvider
                .generateRefreshToken(email);

        // ── Build redirect URL with tokens ────────────────────────
        String targetUrl = UriComponentsBuilder
                .fromUriString(frontendUrl + "/oauth2/callback")
                .queryParam("token",        token)
                .queryParam("refreshToken", refreshToken)
                .queryParam("role",         user.getRole().name())
                .build()
                .toUriString();

        log.info("OAuth2 login success for [{}], redirecting",
                email);

        getRedirectStrategy().sendRedirect(
                request, response, targetUrl);
    }
}