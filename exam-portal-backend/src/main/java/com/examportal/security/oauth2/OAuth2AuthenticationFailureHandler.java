package com.examportal.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.util.UriComponentsBuilder;

import javax.naming.AuthenticationException;
import java.io.IOException;

public class OAuth2AuthenticationFailureHandler  extends SimpleUrlAuthenticationFailureHandler {

    private String frontendUrl;

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/login")
                .queryParam("error",exception.getLocalizedMessage())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request,response,targetUrl);
    }
}
