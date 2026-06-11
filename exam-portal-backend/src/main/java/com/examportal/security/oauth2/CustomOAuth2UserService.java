package com.examportal.security.oauth2;

import com.examportal.model.Role;
import com.examportal.model.User;
import com.examportal.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Slf4j
public class CustomOAuth2UserService  implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId,oAuth2User.getAttributes());

        log.info("OAuth2 login via [{}] for email [{}]",registrationId,userInfo.getEmail());

        userRepository.findByEmail(userInfo.getEmail()).ifPresentOrElse(existingUser -> {
            existingUser.setName(userInfo.getName());
            existingUser.setProfilePicture(userInfo.getImageUrl());
            userRepository.save(existingUser);
        },
                () -> {
                   User newUser = User.builder()
                           .email(userInfo.getEmail())
                           .name(userInfo.getName())
                           .profilePicture(userInfo.getImageUrl())
                           .role(Role.USER)
                           .provider(registrationId)
                           .emailverified(true)
                           .enabled(false)
                           .approved(false)
                           .blocked(false)
                           .build();
                   userRepository.save(newUser);
                   log.info("New OAuth2 user registered : {}", newUser.getEmail());
                }
                );
        return  oAuth2User;
    }
}
