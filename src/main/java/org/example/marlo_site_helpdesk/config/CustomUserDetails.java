package org.example.marlo_site_helpdesk.config;

import org.example.marlo_site_helpdesk.models.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class CustomUserDetails extends
        org.springframework.security.core.userdetails.User {

    private final Long id;

    public CustomUserDetails(User user) {

        super(
                user.getEmail(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name()
                ))
        );

        this.id = user.getId();
    }

    public Long getId() {
        return id;
    }
}