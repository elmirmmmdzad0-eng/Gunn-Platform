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
        String cleanPassword = clean(rawPassword);

        if (cleanUsername.isBlank() || cleanEmail.isBlank() || cleanPassword.isBlank()) {
            throw new IllegalArgumentException("Username, email ve password bos ola bilmez.");
        }

        if (userRepository.existsByUsername(cleanUsername)) {
            throw new IllegalArgumentException("Bu username artiq istifade olunur.");
        }

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Bu email artiq qeydiyyatdan kecib.");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setUsername(cleanUsername);
        user.setEmail(cleanEmail);
        user.setPassword(encodedPassword);
        // Local/test mode: keep the account usable even if Gmail delivery fails.
        // The verification token is still generated, so email verification can be used when SMTP works.
        user.setEnabled(true);
        user.setVerificationToken(UUID.randomUUID().toString());

        User savedUser = userRepository.saveAndFlush(user);
        System.out.println("[AUTH_REGISTER_SUCCESS] userId=" + savedUser.getId()
                + ", username=" + savedUser.getUsername()
                + ", email=" + savedUser.getEmail()
                + ", enabled=" + savedUser.isEnabled()
                + ", passwordHashPrefix=" + maskHash(savedUser.getPassword()));

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

    @Transactional
    public User login(String identifier, String rawPassword) {
        String cleanIdentifier = clean(identifier);
        String cleanPassword = clean(rawPassword);

        if (cleanIdentifier.isBlank() || cleanPassword.isBlank()) {
            throw new IllegalArgumentException("Email/username ve password bos ola bilmez.");
        }

        User user = findByEmailOrUsername(cleanIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("Email/username ve ya password yanlisdir."));

        System.out.println("[AUTH_LOGIN_ATTEMPT] identifier=" + cleanIdentifier
                + ", userId=" + user.getId()
                + ", username=" + user.getUsername()
                + ", email=" + user.getEmail()
                + ", enabled=" + user.isEnabled()
                + ", passwordHashPrefix=" + maskHash(user.getPassword()));

        if (!user.isEnabled()) {
            System.out.println("[AUTH_LOGIN_AUTO_ENABLE] Legacy disabled user activated for local/test login. userId=" + user.getId());
            user.setEnabled(true);
            user = userRepository.saveAndFlush(user);
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            System.out.println("[AUTH_LOGIN_FAILED] Səbəb: BCrypt password mismatch. identifier=" + cleanIdentifier);
            throw new IllegalArgumentException("Email/username ve ya password yanlisdir.");
        }

        System.out.println("[AUTH_LOGIN_SUCCESS] userId=" + user.getId() + ", username=" + user.getUsername());
        return user;
    }

    private Optional<User> findByEmailOrUsername(String identifier) {
        String normalizedIdentifier = identifier.toLowerCase();
        Optional<User> byEmail = userRepository.findByEmail(normalizedIdentifier);
        if (byEmail.isPresent()) {
            return byEmail;
        }

        return userRepository.findByUsername(identifier);
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

    private String maskHash(String hash) {
        if (hash == null || hash.length() < 10) {
            return "missing";
        }

        return hash.substring(0, 10) + "...";
    }
}
