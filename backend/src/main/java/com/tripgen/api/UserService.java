package com.tripgen.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public User registerUser(String username, String email, String rawPassword) {
        String cleanUsername = clean(username);
        String cleanEmail = clean(email).toLowerCase();

        if (cleanUsername.isBlank() || cleanEmail.isBlank() || clean(rawPassword).isBlank()) {
            throw new IllegalArgumentException("Username, email ve password bos ola bilmez.");
        }

        if (userRepository.existsByUsername(cleanUsername)) {
            throw new IllegalArgumentException("Bu username artiq istifade olunur.");
        }

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Bu email artiq qeydiyyatdan kecib.");
        }

        User user = new User();
        user.setUsername(cleanUsername);
        user.setEmail(cleanEmail);
        user.setPassword(passwordEncoder.encode(rawPassword));
        // Local/test mode: keep the account usable even if Gmail delivery fails.
        // The verification token is still generated, so email verification can be used when SMTP works.
        user.setEnabled(true);
        user.setVerificationToken(UUID.randomUUID().toString());

        User savedUser = userRepository.save(user);
        emailService.sendVerificationEmail(
                savedUser.getEmail(),
                savedUser.getUsername(),
                buildVerificationLink(savedUser.getVerificationToken())
        );

        return savedUser;
    }

    @Transactional
    public boolean verifyEmail(String token) {
        Optional<User> optionalUser = userRepository.findByVerificationToken(clean(token));
        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();
        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);
        return true;
    }

    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(clean(email).toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Email ve ya password yanlisdir."));

        if (!user.isEnabled()) {
            throw new IllegalStateException("Hesab aktiv deyil. Zehmet olmasa emailinizi tesdiqleyin.");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Email ve ya password yanlisdir.");
        }

        return user;
    }

    private String buildVerificationLink(String token) {
        String baseUrl = appBaseUrl.endsWith("/") ? appBaseUrl.substring(0, appBaseUrl.length() - 1) : appBaseUrl;
        return baseUrl + "/api/auth/verify?token=" + token;
    }

    private String clean(String value) {
        if (value == null) {
            return "";
        }

        return value.trim();
    }
}
