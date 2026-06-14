package com.library.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing Library Members (Users).
 */
@RestController
@RequestMapping("/api/members")
@Tag(name = "Users", description = "Endpoints for managing library members (Admin/Librarian)")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @Operation(summary = "Get all members", description = "Retrieves a list of all library members")
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @Operation(summary = "Get member by ID", description = "Retrieves details of a specific member")
    public ResponseEntity<User> getById(@PathVariable @NonNull Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new member", description = "Adds a new member to the library system")
    public ResponseEntity<?> create(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username already exists"));
        }

        user.setPassword(passwordEncoder.encode(user.getPassword() != null ? user.getPassword() : "lib@123"));
        user.setStatus("active");
        user.setJoinedDate(LocalDate.now().toString());
        user.setLoans(0);

        logger.info("Admin creating new member: {}", user.getUsername());
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update member", description = "Updates details of an existing member")
    public ResponseEntity<User> update(@PathVariable @NonNull Long id, @RequestBody User updated) {
        return userRepository.findById(id).map(user -> {
            user.setFullname(updated.getFullname());
            user.setEmail(updated.getEmail());
            user.setRole(updated.getRole());
            user.setStatus(updated.getStatus());
            if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(updated.getPassword()));
            }
            User saved = userRepository.save(user);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle member status", description = "Activates or suspends a member account")
    public ResponseEntity<User> toggle(@PathVariable @NonNull Long id) {
        return userRepository.findById(id).map(user -> {
            user.setStatus("active".equals(user.getStatus()) ? "suspended" : "active");
            User saved = userRepository.save(user);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete member", description = "Permanently removes a member from the library")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id) {
        if (!userRepository.existsById(id))
            return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
