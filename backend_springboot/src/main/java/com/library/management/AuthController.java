package com.library.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * REST Controller for Public Authentication (Login / Register).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        logger.info("Registration request for username: {}, password present: {}", user.getUsername(),
                user.getPassword() != null);
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username taken"));
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
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
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String password = req.get("password");

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
