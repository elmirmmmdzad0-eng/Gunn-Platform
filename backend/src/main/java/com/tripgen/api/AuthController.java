package com.tripgen.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request.username(), request.email(), request.password());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Qeydiyyat ugurludur. Profil aktivdir, indi giris ede bilersiniz.");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("enabled", user.isEnabled());
            response.put("emailSent", true);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            System.out.println("[AUTH_REGISTER_FAILED] Səbəb: " + e.getMessage());
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        } catch (Exception e) {
            System.out.println("[AUTH_REGISTER_FAILED] Səbəb: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error("Qeydiyyat zamani xeta bas verdi. Server loglarini yoxlayin."));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(@RequestParam String token) {
        boolean verified = userService.verifyEmail(token);
        if (!verified) {
            return ResponseEntity.badRequest().body(error("Tesdiq linki yanlisdir ve ya vaxti kecib."));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Email ugurla tesdiqlendi. Indi hesaba daxil ola bilersiniz.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.login(request.email(), request.password());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login ugurludur.");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("enabled", user.isEnabled());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[AUTH_LOGIN_FAILED] Səbəb: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error(e.getMessage()));
        } catch (Exception e) {
            System.out.println("[AUTH_LOGIN_FAILED] Səbəb: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error("Login zamani gozlenilmez xeta bas verdi. Server loglarini yoxlayin."));
        }
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        return response;
    }

    public record RegisterRequest(String username, String email, String password) {
    }

    public record LoginRequest(String email, String password) {
    }
}
