package com.example.dailyreport.web;

import com.example.dailyreport.domain.UserAccount;
import com.example.dailyreport.domain.UserRole;
import com.example.dailyreport.repository.UserAccountRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
@RequestMapping("/users")
public class UserController {
    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserAccountRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("user", new UserAccount());
        model.addAttribute("roles", UserRole.values());
        return "users/form";
    }

    @PostMapping
    public String create(@ModelAttribute UserAccount user, Model model) {
        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("user", user);
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("error", "이미 등록된 이메일입니다.");
            return "users/form";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/users";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userRepository.findById(id).orElseThrow());
        model.addAttribute("roles", UserRole.values());
        return "users/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute UserAccount updates) {
        UserAccount user = userRepository.findById(id).orElseThrow();
        user.setName(updates.getName());
        user.setEmail(updates.getEmail());
        user.setRole(updates.getRole());
        if (updates.getPassword() != null && !updates.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(updates.getPassword()));
        }
        userRepository.save(user);
        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/users";
    }
}
