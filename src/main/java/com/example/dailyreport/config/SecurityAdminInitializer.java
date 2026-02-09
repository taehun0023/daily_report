package com.example.dailyreport.config;

import com.example.dailyreport.domain.UserAccount;
import com.example.dailyreport.domain.UserRole;
import com.example.dailyreport.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SecurityAdminInitializer implements CommandLineRunner {
    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.admin-email:admin@example.com}")
    private String adminEmail;

    @Value("${app.security.admin-password:admin1234}")
    private String adminPassword;

    @Value("${app.security.admin-name:관리자}")
    private String adminName;

    @Value("${app.security.temp-password:temp1234}")
    private String tempPassword;

    public SecurityAdminInitializer(UserAccountRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        for (UserAccount user : userRepository.findAll()) {
            boolean changed = false;
            if (user.getRole() == null) {
                user.setRole(UserRole.MANAGER);
                changed = true;
            }
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(tempPassword));
                changed = true;
            }
            if (changed) {
                userRepository.save(user);
            }
        }

        UserAccount admin = userRepository.findByEmail(adminEmail).orElse(null);
        if (admin == null) {
            admin = new UserAccount();
            admin.setEmail(adminEmail);
        }
        admin.setName(adminName);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(UserRole.ADMIN);
        userRepository.save(admin);
    }
}
