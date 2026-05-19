package com.tripgen.api;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/waitlist")
@CrossOrigin(origins = "*")
public class WaitlistController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final WaitlistRepository waitlistRepository;

    public WaitlistController(WaitlistRepository waitlistRepository) {
        this.waitlistRepository = waitlistRepository;
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinWaitlist(
            @RequestBody(required = false) WaitlistJoinRequest request,
            @RequestParam(value = "email", required = false) String emailParam
    ) {
        String rawEmail = request != null && request.email() != null ? request.email() : emailParam;
        String email = normalizeEmail(rawEmail);

        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Valid email is required."));
        }

        if (waitlistRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.ok(Map.of("success", true, "alreadyJoined", true));
        }

        try {
            waitlistRepository.save(new WaitlistEntry(email));
            return ResponseEntity.ok(Map.of("success", true, "alreadyJoined", false));
        } catch (DataIntegrityViolationException duplicate) {
            return ResponseEntity.ok(Map.of("success", true, "alreadyJoined", true));
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }

    public record WaitlistJoinRequest(String email) {
    }
}
