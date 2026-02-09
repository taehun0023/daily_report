package com.example.dailyreport.service;

import com.example.dailyreport.domain.UserAccount;
import com.example.dailyreport.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserAccountDetailsService implements UserDetailsService {
    private final UserAccountRepository userRepository;

    public UserAccountDetailsService(UserAccountRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount account = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        if (account.getPassword() == null || account.getPassword().isBlank() || account.getRole() == null) {
            throw new UsernameNotFoundException("계정 설정이 완료되지 않았습니다.");
        }
        return User.withUsername(account.getEmail())
                .password(account.getPassword())
                .roles(account.getRole().name())
                .build();
    }
}
