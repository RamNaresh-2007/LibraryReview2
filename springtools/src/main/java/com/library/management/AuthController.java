package com.library.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.Map;

/**
 * REST Controller for Public Authentication (Login / Register).
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user login and registration")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new library member account")
    public ResponseEntity<?> register(@RequestBody AuthDTOs.RegisterRequest req) {
        logger.info("Registration request: username={}, email={}, role={}", req.getUsername(), req.getEmail(),
                req.getRole());
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username is required"));
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username taken"));
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setFullname(req.getFullname());
        user.setEmail(req.getEmail());
        user.setRole(req.getRole());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setStatus("active");
        user.setJoinedDate(LocalDate.now().toString());
        user.setLoans(0);

        userRepository.save(user);
        logger.info("New user registered: {}", user.getUsername());

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "fullname", user.getFullname(),
                "email", user.getEmail(),
                "role", user.getRole()));
    }

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticates user and returns JWT token")
    public ResponseEntity<?> login(@RequestBody AuthDTOs.LoginRequest req) {
        String username = req.getUsername();
        String password = req.getPassword();

        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
                    logger.info("User logged in: {}", username);
                    return ResponseEntity.ok(Map.of(
                            "token", token,
                            "username", user.getUsername(),
                            "fullname", user.getFullname(),
                            "email", user.getEmail(),
                            "role", user.getRole()));
                })
                .orElseGet(() -> {
                    logger.warn("Failed login attempt for user: {}", username);
                    return ResponseEntity.status(401).body(Map.of("message", "Invalid username or password"));
                });
    }
}
