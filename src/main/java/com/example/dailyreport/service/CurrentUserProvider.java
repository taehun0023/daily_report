package com.example.dailyreport.service;

import com.example.dailyreport.domain.UserAccount;
import com.example.dailyreport.repository.UserAccountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {
    private final UserAccountRepository userRepository;

    public CurrentUserProvider(UserAccountRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserAccount getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        if (username == null || "anonymousUser".equals(username)) {
            return null;
        }
        return userRepository.findByEmail(username).orElse(null);
    }
}
