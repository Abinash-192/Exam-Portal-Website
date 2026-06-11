package com.examportal.security.oauth2;

import jakarta.validation.ValidationException;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String,Object> attributes){
        return  switch (registrationId.toLowerCase()){
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "github" -> new GithubOAuth2UserInfo(attributes);
            default -> throw new ValidationException("Login with " + registrationId + " is not supported");
        };
    }
}
