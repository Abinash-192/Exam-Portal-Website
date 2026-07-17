package com.examportal.security;

import com.examportal.model.User;
import com.examportal.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("No user found with email : "+email));

        return new org.springframework.security.core.userdetails.User(
                   user.getEmail(),
                   user.getPassword() != null ? user.getPassword() : "",
                   user.isEnabled(),
                   true,
                   true,
                   !user.isBlocked(),
                   List.of(new SimpleGrantedAuthority("ROLE_"+ user.getRole().name()))
                );
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("No user found with id : "+id));
        return loadUserByUsername(user.getEmail());
    }
}
