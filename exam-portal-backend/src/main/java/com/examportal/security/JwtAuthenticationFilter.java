package com.examportal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

//    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService userDetailsService) {
//        this.tokenProvider = tokenProvider;
//        this.userDetailsService = userDetailsService;
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = extractBearerToken(request);
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {

                String email = tokenProvider.getEmailFromToken(jwt);

                if (SecurityContextHolder.getContext().getAuthentication() == null) {

                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authenticated user [{}] for path [{}]",
                            email, request.getServletPath());
                }
            }
        } catch (Exception e) {
            log.error("Failed to set authentication in security context : {}",
                    e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String path = request.getServletPath();
            return path.startsWith("/api/auth/")
                    || path.startsWith("/oauth2/")
                    || path.startsWith("/ws/");
        }

        private String extractBearerToken(HttpServletRequest request){
            String header = request.getHeader("Authorization");
            if(StringUtils.hasText(header) && header.startsWith("Bearer")){
                return header.substring(7);
            }
            return null;
        }

    }

