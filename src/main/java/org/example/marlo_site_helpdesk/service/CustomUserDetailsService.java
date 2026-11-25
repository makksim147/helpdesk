package org.example.marlo_site_helpdesk.service;

import org.example.marlo_site_helpdesk.config.CustomUserDetails;
import org.example.marlo_site_helpdesk.models.User;
import org.example.marlo_site_helpdesk.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Пользователь не найден"
                        ));

        return new CustomUserDetails(user);
    }
}